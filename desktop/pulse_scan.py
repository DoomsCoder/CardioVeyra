"""Timestamped multi-region pulse estimation shared by the desktop scan and tests."""

from collections import deque
from dataclasses import dataclass

import numpy as np

SECOND = 1_000_000_000
WINDOW = 10 * SECOND
MAX_GAP = 200_000_000
RATE = 30
VARIABILITY_WINDOW = 30 * SECOND
# Second order Butterworth prototype, 0.7–4 Hz bandpass, sampled at 30 Hz.
SOS = (
    (0.07909371811698926, 0.15818743623397852, 0.07909371811698926,
     -1.0578730338698499, 0.45331893326639167),
    (1.0, -2.0, 1.0, -1.8104676460327398, 0.8352161615670907),
)


@dataclass(frozen=True)
class Reading:
    bpm: int
    quality: float
    waveform: np.ndarray
    beat_intervals_ms: tuple[float, ...]

    @property
    def sdnn_ms(self):
        return float(np.std(self.beat_intervals_ms, ddof=1)) if len(self.beat_intervals_ms) >= 2 else None

    @property
    def rmssd_ms(self):
        if len(self.beat_intervals_ms) < 2:
            return None
        return float(np.sqrt(np.mean(np.diff(self.beat_intervals_ms) ** 2)))


@dataclass(frozen=True)
class Result:
    bpm: int | None
    elapsed_seconds: int
    quality: str


class PulseScan:
    def __init__(self, *, timed=True):
        self.timed = timed
        self.tracks = {region: deque() for region in ("forehead", "left_cheek", "right_cheek")}
        self.usable = 0
        self.last_accepted = None
        self.accepted = []
        self.interrupt()

    @property
    def progress(self):
        return min(self.usable / (30 * SECOND), 1.0)

    def interrupt(self):
        for track in self.tracks.values():
            track.clear()
        self.previous_time = None
        self.missing = False
        self.previous_regions = set()
        self.last_attempt = None
        self.live = None
        self.variability = None

    def add(self, time_ns, rgb, *, missed_detection=False):
        if not rgb and missed_detection and self.previous_time is not None:
            if 0 < time_ns - self.previous_time <= 120_000_000:
                self.missing = True
                self.live = None
                self.variability = None
                self.last_attempt = time_ns
                return
        if not rgb:
            self.interrupt()
            return
        if self.previous_time is not None:
            dt = time_ns - self.previous_time
            max_gap = 120_000_000 if self.missing else MAX_GAP
            if not 0 < dt <= max_gap:
                self.interrupt()
            elif not self.missing and self.previous_regions.intersection(rgb):
                self.usable += dt
        if self.missing:
            self.last_attempt = None
            self.missing = False
        self.previous_time = time_ns
        self.previous_regions = set(rgb)
        retention = WINDOW if self.timed else VARIABILITY_WINDOW
        for region, track in self.tracks.items():
            if region not in rgb:
                track.clear()
                continue
            track.append((time_ns, rgb[region]))
            while len(track) > 2 and track[1][0] <= time_ns - retention:
                track.popleft()
        if self.last_attempt is not None and time_ns - self.last_attempt < SECOND:
            return
        self.last_attempt = time_ns
        readings = [reading for track in self.tracks.values() if (reading := estimate(track, time_ns))]
        self.live = max(readings, key=lambda reading: reading.quality, default=None)
        if not self.timed:
            variability = [
                reading for track in self.tracks.values()
                if self.live and (reading := estimate(track, time_ns, VARIABILITY_WINDOW, min_rate=18, max_gap=100_000_000))
                and reading.quality >= 0.85 and len(reading.beat_intervals_ms) >= 20
                and abs(reading.bpm - self.live.bpm) <= 5
            ]
            self.variability = max(variability, key=lambda reading: reading.quality, default=None)
            return
        if self.live and (self.last_accepted is None or time_ns - self.last_accepted >= WINDOW):
            self.accepted.append(self.live)
            self.last_accepted = time_ns

    def result(self, elapsed_seconds):
        agreeing = []
        if self.accepted:
            center = np.median([reading.bpm for reading in self.accepted])
            agreeing = [reading for reading in self.accepted
                        if abs(reading.bpm - center) <= max(5, center * 0.08)]
        reliable = len(agreeing) >= 2 and len(agreeing) * 2 > len(self.accepted)
        if elapsed_seconds < 60 and (self.progress < 1 or not reliable):
            return None
        if not reliable:
            return Result(None, elapsed_seconds, "Insufficient")
        quality = "High" if np.mean([reading.quality for reading in agreeing]) >= 0.75 else "Moderate"
        return Result(round(float(np.median([reading.bpm for reading in agreeing]))), elapsed_seconds, quality)


def estimate(track, end, window_ns=WINDOW, min_rate=12, max_gap=MAX_GAP):
    start = end - window_ns
    if len(track) < 2 or track[0][0] > start:
        return None
    timestamps, rgb = zip(*track)
    times = np.asarray(timestamps, dtype=np.int64) - start
    first = max(0, np.searchsorted(times, 0, side="right") - 1)
    times = times[first:]
    rgb = rgb[first:]
    if len(times) < min_rate * window_ns // SECOND or np.any(np.diff(times) > max_gap):
        return None
    count = window_ns * RATE // SECOND
    grid = np.arange(count, dtype=np.int64) * SECOND // RATE
    uniform = np.array([np.interp(grid, times, channel) for channel in np.asarray(rgb).T])

    signal = np.zeros(count)
    coverage = np.zeros(count)
    length = 48
    for offset in range(count - length + 1):
        window = slice(offset, offset + length)
        channels = uniform[:, window]
        means = channels.mean(axis=1, keepdims=True)
        if np.any(means < 1):
            continue
        r, g, b = channels / means
        x, y = g - b, g + b - 2 * r
        denominator = np.linalg.norm(y)
        if denominator < 1e-8 * np.sqrt(length):
            continue
        projected = x + (np.linalg.norm(x) / denominator) * y
        signal[window] += projected - projected.mean()
        coverage[window] += 1
    if np.any(coverage == 0):
        return None
    filtered = signal / coverage
    for b0, b1, b2, a1, a2 in SOS:
        output = np.empty_like(filtered)
        z1 = z2 = 0.0
        for i, value in enumerate(filtered):
            output[i] = value * b0 + z1
            z1 = value * b1 - output[i] * a1 + z2
            z2 = value * b2 - output[i] * a2
        filtered = output
    filtered = filtered[30:]
    amplitude = np.sqrt(np.mean(filtered ** 2))
    if amplitude < 0.00008:
        return None

    peaks = []
    for index in range(1, len(filtered) - 1):
        value = filtered[index]
        if value <= amplitude * 0.4 or value <= filtered[index - 1] or value < filtered[index + 1]:
            continue
        if not peaks or index - peaks[-1] >= 8:
            peaks.append(index)
        elif value > filtered[peaks[-1]]:
            peaks[-1] = index
    refined = []
    for index in peaks:
        left, center, right = filtered[index - 1:index + 2]
        curvature = left - 2 * center + right
        offset = 0 if abs(curvature) < 1e-12 else np.clip((left - right) / (2 * curvature), -0.5, 0.5)
        refined.append(index + offset)
    intervals = np.diff(refined)
    if intervals.size < 5:
        return None
    period = float(np.median(intervals))
    if not 10 <= period <= 42:
        return None
    regular = np.mean(np.abs(intervals - period) <= 0.20 * period)
    lag = int(period + 0.5)
    a, b = filtered[lag:], filtered[:-lag]
    correlation = float(np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b)))
    if not np.isfinite(correlation) or correlation < 0.55 or regular < 0.70:
        return None
    evaluation = tuple(float(value * 1000 / RATE) for value in intervals) if regular == 1 else ()
    return Reading(int(60 * RATE / period + 0.5), (correlation + regular) / 2,
                   np.clip(filtered / (amplitude * 3), -1, 1), evaluation)
