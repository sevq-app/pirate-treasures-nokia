import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import java.util.Random;

public final class MaratTreasures extends MIDlet {
 private Game game;
 public void startApp(){ if(game==null)game=new Game(this); Display.getDisplay(this).setCurrent(game); }
 public void pauseApp(){}
 public void destroyApp(boolean unconditional){ if(game!=null)game.save(); }
 void quit(){destroyApp(true);notifyDestroyed();}

 static final class Game extends Canvas {
  private final MaratTreasures app;
  private final Random rng=new Random();
  private final int[][] board=new int[6][6];
  private int level=1,coins=500000,moves=20,target=18,score=0,cx=0,cy=0,sx=-1,sy=-1,mode=0;
  private boolean won=false;
  private static final int[] COLORS={0xED4B53,0x49C8F2,0xE8BD45,0x73D06C,0xAD76E9,0xF48D3A};
  Game(MaratTreasures a){app=a;setFullScreenMode(true);load();newBoard();}
  private void newBoard(){for(int y=0;y<6;y++)for(int x=0;x<6;x++){int v;do{v=rng.nextInt(6);}while((x>1&&board[y][x-1]==v&&board[y][x-2]==v)||(y>1&&board[y-1][x]==v&&board[y-2][x]==v));board[y][x]=v;}moves=20+level/12;target=18+level/5;score=0;won=false;sx=-1;mode=0;repaint();}
  protected void paint(Graphics g){
   int w=getWidth(),h=getHeight();g.setColor(0x10283D);g.fillRect(0,0,w,h);
   g.setColor(0xFFFFFF);g.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD,Font.SIZE_SMALL));
   g.drawString("Сокровища Марата",w/2,2,Graphics.TOP|Graphics.HCENTER);
   g.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_SMALL));
   if(mode==1){g.drawString("Уровень пройден!",w/2,45,Graphics.TOP|Graphics.HCENTER);g.drawString("OK - дальше",w/2,70,Graphics.TOP|Graphics.HCENTER);return;}
   if(mode==2){g.drawString("Ходы закончились",w/2,45,Graphics.TOP|Graphics.HCENTER);g.drawString("OK - повторить",w/2,70,Graphics.TOP|Graphics.HCENTER);return;}
   g.drawString(new StringBuffer().append("Ур.").append(level).append("  ").append(coins).append(" мон.").toString(),2,18,Graphics.TOP|Graphics.LEFT);
   g.drawString(new StringBuffer().append("Ходы:").append(moves).append("  ").append(score).append("/").append(target).toString(),2,31,Graphics.TOP|Graphics.LEFT);
   int cell=Math.min((w-8)/6,(h-62)/6);if(cell<8)cell=8;
   int ox=(w-cell*6)/2,oy=46;
   for(int y=0;y<6;y++)for(int x=0;x<6;x++){
    int px=ox+x*cell,py=oy+y*cell;
    g.setColor(0x24465A);g.fillRect(px,py,cell-1,cell-1);
    g.setColor(COLORS[board[y][x]]);g.fillRect(px+3,py+3,cell-7,cell-7);
    if(x==cx&&y==cy){g.setColor(0xFFFFFF);g.drawRect(px+1,py+1,cell-3,cell-3);}
    if(x==sx&&y==sy){g.setColor(0xFFDD55);g.drawRect(px+2,py+2,cell-5,cell-5);}
   }
   g.setColor(0xFFFFFF);g.drawString("OK: выбор  0: заново",w/2,h-14,Graphics.TOP|Graphics.HCENTER);
  }
  protected void keyPressed(int key){
   if(mode!=0){if(key==Canvas.FIRE||key==53||key==-5){if(mode==1){level++;coins+=100;}newBoard();save();}return;}
   if(key==48){newBoard();return;}
   int action=0;try{action=getGameAction(key);}catch(Exception e){}
   if(key==50||action==UP)cy=(cy+5)%6;
   else if(key==56||action==DOWN)cy=(cy+1)%6;
   else if(key==52||action==LEFT)cx=(cx+5)%6;
   else if(key==54||action==RIGHT)cx=(cx+1)%6;
   else if(key==53||action==FIRE||key==-5){select();}
   repaint();
  }
  private void select(){
   if(sx<0){sx=cx;sy=cy;return;}
   if(sx==cx&&sy==cy){sx=-1;return;}
   if(Math.abs(sx-cx)+Math.abs(sy-cy)!=1){sx=cx;sy=cy;return;}
   swap(sx,sy,cx,cy);
   if(!hasMatch()){swap(sx,sy,cx,cy);}else{moves--;resolve();if(score>=target){mode=1;won=true;}else if(moves<=0)mode=2;}
   sx=-1;save();
  }
  private void swap(int x1,int y1,int x2,int y2){int t=board[y1][x1];board[y1][x1]=board[y2][x2];board[y2][x2]=t;}
  private boolean hasMatch(){for(int y=0;y<6;y++)for(int x=0;x<6;x++){int v=board[y][x];if(x<4&&v==board[y][x+1]&&v==board[y][x+2])return true;if(y<4&&v==board[y+1][x]&&v==board[y+2][x])return true;}return false;}
  private void resolve(){
   for(int loop=0;loop<30;loop++){
    boolean[][] mark=new boolean[6][6];int count=0;
    for(int y=0;y<6;y++)for(int x=0;x<6;x++){
     int v=board[y][x];if(x<4&&v==board[y][x+1]&&v==board[y][x+2]){int i=x;while(i<6&&board[y][i]==v)mark[y][i++]=true;}
     if(y<4&&v==board[y+1][x]&&v==board[y+2][x]){int i=y;while(i<6&&board[i][x]==v)mark[i++][x]=true;}
    }
    for(int y=0;y<6;y++)for(int x=0;x<6;x++)if(mark[y][x])count++;
    if(count==0)break;score+=count;
    for(int x=0;x<6;x++){int dest=5;for(int y=5;y>=0;y--)if(!mark[y][x])board[dest--][x]=board[y][x];while(dest>=0)board[dest--][x]=rng.nextInt(6);}
   }
  }
  void save(){try{RecordStore rs=RecordStore.openRecordStore("marat",true);byte[] data=new byte[]{(byte)(level>>8),(byte)level,(byte)(coins>>24),(byte)(coins>>16),(byte)(coins>>8),(byte)coins};if(rs.getNumRecords()==0)rs.addRecord(data,0,data.length);else rs.setRecord(1,data,0,data.length);rs.closeRecordStore();}catch(Exception e){}}
  void load(){try{RecordStore rs=RecordStore.openRecordStore("marat",true);if(rs.getNumRecords()>0){byte[] d=rs.getRecord(1);if(d.length>=6){level=((d[0]&255)<<8)|(d[1]&255);coins=((d[2]&255)<<24)|((d[3]&255)<<16)|((d[4]&255)<<8)|(d[5]&255);if(level<1||level>1000)level=1;}}rs.closeRecordStore();}catch(Exception e){}}
 }
}
