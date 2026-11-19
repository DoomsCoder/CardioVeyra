import math
import unittest

from pulse_scan import PulseScan


def trace(scan, start, end, pulse=True, region="forehead", bpm=72):
    time_ns = int(start * 1e9)
    while time_ns <= end * 1e9:
        seconds = time_ns / 1e9
        motion = 1 + 0.03 * math.sin(2 * math.pi * 0.18 * seconds)
        signal = math.sin(2 * math.pi * bpm / 60 * seconds) if pulse else 0
        rgb = (130 * motion * (1 + 0.003 * signal),
               105 * motion * (1 + 0.012 * signal),
               80 * motion * (1 + 0.004 * signal))
        scan.add(time_ns, {region: rgb})
        time_ns += 41_000_000 + (time_ns // 1_000_000 % 7) * 1_000_000


class PulseScanTest(unittest.TestCase):
    def test_live_waveform_and_final_bpm(self):
        scan = PulseScan()
        trace(scan, 0, 13)
        self.assertLessEqual(abs(scan.live.bpm - 72), 2)
        self.assertEqual(len(scan.live.waveform), 270)
        self.assertLess(scan.live.sdnn_ms, 20)
        self.assertLess(scan.live.rmssd_ms, 20)
        trace(scan, 13.02, 32)
        result = scan.result(32)
        self.assertLessEqual(abs(result.bpm - 72), 2)
        self.assertEqual(result.elapsed_seconds, 32)
        self.assertEqual(result.quality, "High")

    def test_roi_switch_and_tracking_loss_start_new_windows(self):
        scan = PulseScan()
        trace(scan, 0, 7)
        trace(scan, 7.02, 14, region="left_cheek")
        self.assertIsNone(scan.live)
        scan.add(14_100_000_000, {})
        progress = scan.progress
        trace(scan, 20, 26, region="left_cheek")
        self.assertIsNone(scan.live)
        self.assertLess(scan.progress - progress, 0.21)
        trace(scan, 26.02, 32, region="left_cheek")
        self.assertLessEqual(abs(scan.live.bpm - 72), 2)

    def test_common_illumination_without_pulse_prompts_rescan(self):
        scan = PulseScan()
        trace(scan, 0, 31, pulse=False)
        self.assertIsNone(scan.live)
        self.assertIsNone(scan.result(59))
        self.assertIsNone(scan.result(60).bpm)
        self.assertEqual(scan.result(60).quality, "Insufficient")

    def test_disagreeing_independent_windows_are_rejected(self):
        scan = PulseScan()
        trace(scan, 0, 12, bpm=72)
        scan.interrupt()
        trace(scan, 13, 26, bpm=108)
        self.assertIsNone(scan.result(60).bpm)


if __name__ == "__main__":
    unittest.main()
