import unittest
from types import SimpleNamespace

import numpy as np

from face_tracking import FaceTracker, Quality


class FaceTrackingTest(unittest.TestCase):
    def setUp(self):
        self.tracker = FaceTracker()
        self.image = np.full((480, 640, 3), [145, 105, 75], dtype=np.uint8)
        points = {10: (320, 112), 33: (244, 192), 133: (276, 192), 50: (246, 243),
                  362: (364, 192), 263: (396, 192), 280: (394, 243)}
        self.face = [SimpleNamespace(x=x / 640, y=y / 480) for x, y in
                     (points.get(index, (320, 240)) for index in range(468))]
        self.transform = np.eye(4)

    def evaluate(self, ms, image=None, faces=None):
        return self.tracker.evaluate(
            self.image if image is None else image,
            [self.face] if faces is None else faces,
            [self.transform],
            ms * 1_000_000,
        )

    def test_three_independent_rgb_regions_and_partial_occlusion(self):
        self.evaluate(0)
        self.evaluate(150)
        result = self.evaluate(300)
        self.assertEqual(result.quality, Quality.OPTIMAL)
        self.assertEqual(set(result.rgb), {"forehead", "left_cheek", "right_cheek"})

        covered = self.image.copy()
        covered[212:273, 215:277] = 0
        result = self.evaluate(330, image=covered)
        self.assertEqual(result.quality, Quality.OPTIMAL)
        self.assertEqual(set(result.rgb), {"forehead", "left_cheek"})
        self.assertEqual(result.rgb["forehead"], (145, 105, 75))

        result = self.evaluate(360, image=np.zeros_like(self.image))
        self.assertEqual(result.quality, Quality.POOR_LIGHTING)
        self.assertFalse(result.rgb)

    def test_multiple_faces_and_gaps_reset_stability(self):
        self.assertEqual(self.evaluate(0).quality, Quality.MOTION_WARNING)
        self.evaluate(150)
        self.assertTrue(self.evaluate(300).rgb)
        self.assertEqual(self.evaluate(330, faces=[self.face, self.face]).quality, Quality.MULTIPLE_FACES)
        self.assertFalse(self.evaluate(360).rgb)
        self.evaluate(520)
        self.assertTrue(self.evaluate(680).rgb)
        self.assertFalse(self.evaluate(1000).rgb)
        self.assertEqual(self.evaluate(1030, faces=[]).quality, Quality.FACE_LOST)


if __name__ == "__main__":
    unittest.main()
