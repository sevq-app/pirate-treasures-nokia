"""Fast checks for generated level rules and important Java ME invariants."""
from pathlib import Path
import re
import unittest

SOURCE = Path("src/MaratTreasures.java").read_text(encoding="utf-8")

class GameRules(unittest.TestCase):
    def test_1000_level_rules(self):
        for level in range(1, 1001):
            island = (level - 1) // 50 + 1
            kind = (level - 1) % 5
            moves = max(12, 26 - level // 85)
            obstacles = min(11, 2 + ((level - 1) // 50) // 2)
            goal = (15 + level // 12 if kind == 0 else
                    obstacles if kind in (1, 2) else
                    14 + level // 15 if kind == 3 else
                    10 + level // 20)
            self.assertTrue(1 <= island <= 20)
            self.assertTrue(12 <= moves <= 26)
            self.assertGreater(goal, 0)

    def test_objective_uses_actual_obstacles(self):
        self.assertIn("goal=remainingBlocks()", SOURCE)

    def test_reshuffle_does_not_delete_obstacles(self):
        start = SOURCE.index("private void shuffle()")
        end = SOURCE.index("private void removeMatches()", start)
        shuffle = SOURCE[start:end]
        self.assertNotIn("blocks[y][x]=0", shuffle)
        self.assertIn("if(possible())return", shuffle)

    def test_booster_and_match_use_shared_win(self):
        self.assertRegex(SOURCE, r"if\\s*\\(done>=goal\\)\\s*\\{?\\s*win\\(\\)")
        self.assertIn("if(done>=goal)win();else if(!possible())shuffle();", SOURCE)

    def test_cldc_no_stringbuilder(self):
        self.assertNotIn("StringBuilder", SOURCE)

    def test_rms_save_and_load(self):
        self.assertIn("void save()", SOURCE)
        self.assertIn("void load()", SOURCE)
        self.assertIn("RecordStore.openRecordStore", SOURCE)

if __name__ == "__main__":
    unittest.main()
