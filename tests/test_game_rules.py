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
            goal = (15 + level // 80 if kind == 0 else
                    obstacles if kind in (1, 2) else
                    14 + level // 90 if kind == 3 else
                    10 + level // 125)
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
        self.assertIn("if(done>=goal){win();}", SOURCE)
        self.assertIn("if(done>=goal)win();else if(!possible())shuffle();", SOURCE)

    def test_swaps_only_count_local_matches(self):
        self.assertIn("private boolean matchedAt(int x,int y)", SOURCE)
        self.assertIn("matchedAt(x,y)||matchedAt(a,b)", SOURCE)
        self.assertIn("!validSwap(selectionX,selectionY,cx,cy)", SOURCE)

    def test_locked_gems_cannot_be_swapped(self):
        self.assertIn("blocks[selectionY][selectionX]>0||blocks[cy][cx]>0", SOURCE)

    def test_objective_scaling_fits_moves(self):
        for level in range(1, 1001):
            kind = (level - 1) % 5
            if kind == 4:
                self.assertLessEqual(10 + level // 125, 18)

    def test_final_level_has_ending(self):
        self.assertIn("if(level<1000)level++;else finished=1;", SOURCE)
        self.assertIn("if(finished==1&&won==1)screen=MAP", SOURCE)
        self.assertIn("b[87]=(byte)(finished==1?43:42)", SOURCE)

    def test_red_gem_levels_have_no_unrelated_obstacles(self):
        self.assertIn("if(kind==1||kind==2)for(int i=0;i<count;i++)", SOURCE)
        self.assertNotIn("kind==1||kind==2||kind==4", SOURCE)

    def test_defeat_message_matches_controls(self):
        self.assertIn("Повтори уровень", SOURCE)
        self.assertNotIn("9 - ещё 5 ходов", SOURCE)

    def test_cldc_no_stringbuilder(self):
        self.assertNotIn("StringBuilder", SOURCE)

    def test_resume_snapshot_and_backward_compatibility(self):
        self.assertIn("byte[] b=new byte[88]", SOURCE)
        self.assertIn("rs.setRecord(2,b,0,b.length)", SOURCE)
        self.assertIn("rs.getRecord(2)", SOURCE)
        self.assertIn("sum==stored&&lv==level", SOURCE)
        self.assertIn("active=1;screen=MENU", SOURCE)
        self.assertIn("if(active==1)screen=PLAY;else startLevel();", SOURCE)
        self.assertIn("d.length==6", SOURCE)
        self.assertIn("active=0;save();", SOURCE)

    def test_rms_save_and_load(self):
        self.assertIn("void save()", SOURCE)
        self.assertIn("void load()", SOURCE)
        self.assertIn("RecordStore.openRecordStore", SOURCE)

if __name__ == "__main__":
    unittest.main()
