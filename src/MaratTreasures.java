import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import java.util.Random;

/** Сокровища Марата: автономная игра Java ME, 1000 воспроизводимых уровней. */
public final class MaratTreasures extends MIDlet {
 private Game game;
 public void startApp(){if(game==null)game=new Game(this);Display.getDisplay(this).setCurrent(game);}
 public void pauseApp(){if(game!=null)game.save();}
 public void destroyApp(boolean unconditional){if(game!=null)game.save();}
 void quit(){destroyApp(true);notifyDestroyed();}

 static final class Game extends Canvas {
  private final MaratTreasures app;
  private final Random random=new Random();
  private final int[][] gems=new int[6][6],blocks=new int[6][6];
  private final int[] palette={0xE94E61,0x3ABBE8,0xE7BE45,0x68CE7B,0xA97DE9,0xF59A48};
  private int level=1,coins=500000,stars=0,moves,goal,done,kind,selectionX=-1,selectionY=-1,cx=2,cy=2;
  private int screen=0,tools=0,won=0,active=0,finished=0;
  private String notice="";
  private static final int MENU=0,PLAY=1,RESULT=2,SHOP=3,MAP=4,HELP=5;
  Game(MaratTreasures a){app=a;setFullScreenMode(true);load();}
  private int max(int a,int b){return a>b?a:b;}
  private int min(int a,int b){return a<b?a:b;}
  private int rng(int bound){return random.nextInt(bound);}
  private int difficulty(){return (level-1)/50;}
  private void startLevel(){
   random.setSeed(81291L+level*13271L);
   kind=(level-1)%5;goal=kind==0?15+level/80:kind==1?8:kind==2?6:kind==3?14+level/90:10+level/125;
   moves=max(12,26-level/85);done=0;selectionX=-1;selectionY=-1;cx=2;cy=2;won=0;tools=0;
   for(int y=0;y<6;y++)for(int x=0;x<6;x++){
    int v;do{v=rng(6);}while((x>1&&gems[y][x-1]==v&&gems[y][x-2]==v)||(y>1&&gems[y-1][x]==v&&gems[y-2][x]==v));
    gems[y][x]=v;blocks[y][x]=0;
   }
   // Every fifth level introduces crates, every third level chains.
   int count=min(11,2+difficulty()/2);
   if(kind==1||kind==2)for(int i=0;i<count;i++){
    int x=rng(6),y=rng(6);blocks[y][x]=kind==2?2:1;
   }
   if(kind==1||kind==2){goal=remainingBlocks();}
   if(!possible())shuffle();
   screen=PLAY;active=1;save();repaint();
  }
  private int remainingBlocks(){int n=0;for(int y=0;y<6;y++)for(int x=0;x<6;x++)if(blocks[y][x]>0)n++;return n;}
  private boolean match(){
   for(int y=0;y<6;y++)for(int x=0;x<6;x++){
    int v=gems[y][x];if(v<0)continue;
    if(x<4&&v==gems[y][x+1]&&v==gems[y][x+2])return true;
    if(y<4&&v==gems[y+1][x]&&v==gems[y+2][x])return true;
   }return false;
  }
  private boolean matchedAt(int x,int y){
   int v=gems[y][x],n=1;
   for(int a=x-1;a>=0&&gems[y][a]==v;a--)n++;
   for(int a=x+1;a<6&&gems[y][a]==v;a++)n++;
   if(n>=3)return true;
   n=1;
   for(int a=y-1;a>=0&&gems[a][x]==v;a--)n++;
   for(int a=y+1;a<6&&gems[a][x]==v;a++)n++;
   return n>=3;
  }
  private boolean validSwap(int x,int y,int a,int b){
   swap(x,y,a,b);
   boolean valid=matchedAt(x,y)||matchedAt(a,b);
   swap(x,y,a,b);
   return valid;
  }
  private void swap(int x,int y,int a,int b){int v=gems[y][x];gems[y][x]=gems[b][a];gems[b][a]=v;}
  private boolean possible(){
   for(int y=0;y<6;y++)for(int x=0;x<6;x++){
    if(x<5&&blocks[y][x]==0&&blocks[y][x+1]==0){if(validSwap(x,y,x+1,y))return true;}
    if(y<5&&blocks[y][x]==0&&blocks[y+1][x]==0){if(validSwap(x,y,x,y+1))return true;}
   }return false;
  }
  private void shuffle(){
   // Rebuild without automatic matches. Do not erase mission obstacles.
   for(int attempt=0;attempt<150;attempt++){
    for(int y=0;y<6;y++)for(int x=0;x<6;x++){
     int v;do{v=rng(6);}while((x>1&&gems[y][x-1]==v&&gems[y][x-2]==v)||(y>1&&gems[y-1][x]==v&&gems[y-2][x]==v));
     gems[y][x]=v;
    }
    if(possible())return;
   }
   // No playable swaps: give the player a free reshuffle on next move.
   notice="Нет ходов: перемешай";
  }
  private void removeMatches(){
   for(int round=0;round<24;round++){
    boolean[][] marked=new boolean[6][6];int count=0;
    for(int y=0;y<6;y++)for(int x=0;x<6;x++){
     int v=gems[y][x];if(v<0)continue;
     if(x<4&&v==gems[y][x+1]&&v==gems[y][x+2]){int k=x;while(k<6&&gems[y][k]==v)marked[y][k++]=true;}
     if(y<4&&v==gems[y+1][x]&&v==gems[y+2][x]){int k=y;while(k<6&&gems[k][x]==v)marked[k++][x]=true;}
    }
    for(int y=0;y<6;y++)for(int x=0;x<6;x++)if(marked[y][x]){
     count++;if(kind==0||kind==3)done++;
     if(kind==4&&gems[y][x]==0)done++;
     if(blocks[y][x]>0){blocks[y][x]--;if(blocks[y][x]==0&&(kind==1||kind==2))done++;}
     if(x>0&&blocks[y][x-1]>0){blocks[y][x-1]--;if(blocks[y][x-1]==0&&(kind==1||kind==2))done++;}
     if(x<5&&blocks[y][x+1]>0){blocks[y][x+1]--;if(blocks[y][x+1]==0&&(kind==1||kind==2))done++;}
     if(y>0&&blocks[y-1][x]>0){blocks[y-1][x]--;if(blocks[y-1][x]==0&&(kind==1||kind==2))done++;}
     if(y<5&&blocks[y+1][x]>0){blocks[y+1][x]--;if(blocks[y+1][x]==0&&(kind==1||kind==2))done++;}
    }
    if(count==0)break;
    for(int x=0;x<6;x++){int dest=5;for(int y=5;y>=0;y--)if(!marked[y][x])gems[dest--][x]=gems[y][x];while(dest>=0)gems[dest--][x]=rng(6);}
   }
   if(done>=goal){win();}
   else if(moves<=0){won=0;screen=RESULT;active=0;save();}
   else if(!possible())shuffle();
  }
  private void choose(){
   if(!possible()){shuffle();notice="Перемешано";selectionX=-1;save();return;}
   if(blocks[cy][cx]>0){notice="Фишка закрыта";selectionX=-1;return;}
   if(selectionX<0){selectionX=cx;selectionY=cy;return;}
   if(selectionX==cx&&selectionY==cy){selectionX=-1;return;}
   if(Math.abs(selectionX-cx)+Math.abs(selectionY-cy)!=1){selectionX=cx;selectionY=cy;return;}
   if(blocks[selectionY][selectionX]>0||blocks[cy][cx]>0){notice="Фишка закрыта";}
   else if(!validSwap(selectionX,selectionY,cx,cy)){notice="Нет комбинации";}
   else{swap(selectionX,selectionY,cx,cy);moves--;notice="";removeMatches();}
   selectionX=-1;save();
  }
  private void win(){
   if(screen==RESULT)return;
   won=1;screen=RESULT;active=0;coins+=100+min(400,moves*20);
   stars+=moves>8?3:moves>3?2:1;
   if(level<1000)level++;else finished=1;
   save();
  }
  private void useTool(){
   if(tools==1){if(coins<100){notice="Мало монет";return;}coins-=100;gems[cy][cx]=rng(6);if(blocks[cy][cx]>0){blocks[cy][cx]=0;if(kind==1||kind==2)done++;}tools=0;notice="Молоток";}
   else if(tools==2){if(coins<150){notice="Мало монет";return;}coins-=150;for(int y=max(0,cy-1);y<=min(5,cy+1);y++)for(int x=max(0,cx-1);x<=min(5,cx+1);x++){gems[y][x]=rng(6);if(blocks[y][x]>0){blocks[y][x]=0;if(kind==1||kind==2)done++;}}tools=0;notice="Бомба";}
   else if(tools==3){if(coins<500){notice="Мало монет";return;}coins-=500;moves+=5;tools=0;notice="+5 ходов";}
   if(done>=goal)win();else if(!possible())shuffle();save();
  }
  protected void keyPressed(int key){
   int act=0;try{act=getGameAction(key);}catch(Exception e){}
   boolean ok=key==53||key==-5||act==FIRE;
   boolean up=key==50||act==UP,down=key==56||act==DOWN,left=key==52||act==LEFT,right=key==54||act==RIGHT;
   if(screen==MENU){if(ok){if(active==1)screen=PLAY;else if(finished==1)screen=MAP;else startLevel();}else if(key==49)screen=MAP;else if(key==51)screen=SHOP;else if(key==55)screen=HELP;else if(key==48)app.quit();}
   else if(screen==PLAY){
    if(key==48){selectionX=-1;tools=0;screen=MENU;}
    else if(key==49){tools=1;notice="Молоток 100: OK";}
    else if(key==51){tools=2;notice="Бомба 150: OK";}
    else if(key==57){tools=3;notice="+5 ходов 500: OK";}
    else if(up)cy=max(0,cy-1);else if(down)cy=min(5,cy+1);
    else if(left)cx=max(0,cx-1);else if(right)cx=min(5,cx+1);
    else if(ok){if(tools!=0)useTool();else choose();}
   }
   else if(screen==RESULT){if(ok){if(finished==1&&won==1)screen=MAP;else startLevel();}else if(key==48)screen=MENU;}
   else if(screen==SHOP){if(key==48||ok)screen=MENU;}
   else if(screen==MAP||screen==HELP){if(ok||key==48)screen=MENU;}
   repaint();
  }
  private void line(Graphics g,String s,int y){g.drawString(s,getWidth()/2,y,Graphics.TOP|Graphics.HCENTER);}
  protected void paint(Graphics g){
   int w=getWidth(),h=getHeight();g.setColor(0x12283C);g.fillRect(0,0,w,h);
   g.setColor(0xF7D98A);g.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD,Font.SIZE_SMALL));line(g,"Сокровища Марата",2);
   g.setFont(Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,Font.SIZE_SMALL));g.setColor(0xFFFFFF);
   if(screen==MENU){line(g,"Пиратские острова",25);line(g,new StringBuffer("Уровень ").append(level).append(" / 1000").toString(),45);line(g,new StringBuffer().append(coins).append(" монет").toString(),60);line(g,"OK - Играть",82);line(g,"1 - Острова",98);line(g,"3 - Бонусы  7 - Помощь",114);line(g,"0 - Выход",133);return;}
   if(screen==RESULT){line(g,won==1?(finished==1?"1000 УРОВНЕЙ!":"ПОБЕДА!"):"ХОДЫ КОНЧИЛИСЬ",38);line(g,won==1?(finished==1?"Все острова твои!":"Сундук открыт!"):"Попробуй ещё!",59);line(g,won==1?"+ монеты!":"Повтори уровень",79);line(g,finished==1&&won==1?"OK - карта":"OK - продолжить",104);line(g,"0 - меню",123);return;}
   if(screen==MAP){line(g,"20 островов по 50",30);line(g,new StringBuffer("Остров ").append((level-1)/50+1).toString(),52);line(g,new StringBuffer("Уровень ").append(level).append(" / 1000").toString(),70);line(g,new StringBuffer("Звёзды: ").append(stars).toString(),90);line(g,"OK - назад",121);return;}
   if(screen==SHOP){line(g,"Молоток: 100 мон.",33);line(g,"Бомба: 150 мон.",53);line(g,"5 ходов: 500 мон.",73);line(g,"В игре: 1, 3, 9",94);line(g,"0 - назад",121);return;}
   if(screen==HELP){line(g,"2 4 6 8 - движение",30);line(g,"5 / OK - выбор",49);line(g,"Меняй соседние",68);line(g,"Собирай 3 и больше",87);line(g,"1 молоток 3 бомба",106);line(g,"9 пять ходов 0 меню",125);return;}
   String title=kind==0?"Кристаллы":kind==1?"Сундуки":kind==2?"Цепи":kind==3?"Собери фишки":"Красные камни";
   g.drawString(new StringBuffer("Ур.").append(level).append(" ").append(coins).append("м").toString(),2,17,Graphics.TOP|Graphics.LEFT);
   g.drawString(new StringBuffer(title).append(" ").append(done).append("/").append(goal).toString(),2,30,Graphics.TOP|Graphics.LEFT);
   g.drawString(new StringBuffer("Х:").append(moves).toString(),w-2,30,Graphics.TOP|Graphics.RIGHT);
   int cell=min((w-8)/6,(h-67)/6);int ox=(w-cell*6)/2,oy=44;
   for(int y=0;y<6;y++)for(int x=0;x<6;x++){
    int px=ox+x*cell,py=oy+y*cell;g.setColor(0x315169);g.fillRect(px,py,cell-1,cell-1);
    g.setColor(palette[gems[y][x]]);g.fillRect(px+3,py+3,cell-7,cell-7);
    if(blocks[y][x]>0){g.setColor(blocks[y][x]==1?0xA67849:0xC4D0DC);g.drawRect(px+2,py+2,cell-5,cell-5);g.drawLine(px+2,py+2,px+cell-3,py+cell-3);}
    if(x==cx&&y==cy){g.setColor(0xFFFFFF);g.drawRect(px+1,py+1,cell-3,cell-3);}
    if(x==selectionX&&y==selectionY){g.setColor(0xFFE040);g.drawRect(px+2,py+2,cell-5,cell-5);}
   }
   g.setColor(0xFFFFFF);line(g,notice.length()>0?notice:"1 мол. 3 бом. 9 +5",h-17);
  }
  // Record 1 keeps legacy progress; record 2 stores an optional live board.
  void save(){
   RecordStore rs=null;
   try{
    rs=RecordStore.openRecordStore("marat",true);
    byte[] d=new byte[12];d[0]=1;d[1]=(byte)(level>>8);d[2]=(byte)level;
    d[3]=(byte)(coins>>24);d[4]=(byte)(coins>>16);d[5]=(byte)(coins>>8);d[6]=(byte)coins;
    d[7]=(byte)(stars>>24);d[8]=(byte)(stars>>16);d[9]=(byte)(stars>>8);d[10]=(byte)stars;
    d[11]=(byte)(level+coins+stars);
    if(rs.getNumRecords()==0)rs.addRecord(d,0,d.length);else rs.setRecord(1,d,0,d.length);
    // Fixed 88-byte snapshot: version, active, level, moves, goal, done,
    // kind, cursor, 36 gems, 36 blocks, checksum.
    byte[] b=new byte[88];b[0]=2;b[1]=(byte)active;
    b[2]=(byte)(level>>8);b[3]=(byte)level;
    b[4]=(byte)(moves>>8);b[5]=(byte)moves;
    b[6]=(byte)(goal>>8);b[7]=(byte)goal;
    b[8]=(byte)(done>>8);b[9]=(byte)done;
    b[10]=(byte)kind;b[11]=(byte)cx;b[12]=(byte)cy;
    int p=13;
    for(int y=0;y<6;y++)for(int x=0;x<6;x++)b[p++]=(byte)gems[y][x];
    for(int y=0;y<6;y++)for(int x=0;x<6;x++)b[p++]=(byte)blocks[y][x];
    int sum=0;for(int i=0;i<85;i++)sum=(sum+(b[i]&255))&65535;
    b[85]=(byte)(sum>>8);b[86]=(byte)sum;b[87]=(byte)(finished==1?43:42);
    if(rs.getNumRecords()<2)rs.addRecord(b,0,b.length);else rs.setRecord(2,b,0,b.length);
   }catch(Exception e){}finally{if(rs!=null)try{rs.closeRecordStore();}catch(Exception e){}}
  }
  void load(){
   RecordStore rs=null;
   try{
    rs=RecordStore.openRecordStore("marat",true);
    if(rs.getNumRecords()>0){
     byte[] d=rs.getRecord(1);
     if(d.length>=12&&d[0]==1){
      int lv=((d[1]&255)<<8)|(d[2]&255);
      int c=((d[3]&255)<<24)|((d[4]&255)<<16)|((d[5]&255)<<8)|(d[6]&255);
      int st=((d[7]&255)<<24)|((d[8]&255)<<16)|((d[9]&255)<<8)|(d[10]&255);
      if(d[11]==(byte)(lv+c+st)&&lv>=1&&lv<=1000&&c>=0){level=lv;coins=c;stars=max(0,st);}
     }else if(d.length==6){
      int lv=((d[0]&255)<<8)|(d[1]&255);
      int c=((d[2]&255)<<24)|((d[3]&255)<<16)|((d[4]&255)<<8)|(d[5]&255);
      if(lv>=1&&lv<=1000&&c>=0){level=lv;coins=c;}
     }
    }
    if(rs.getNumRecords()>1){
     byte[] b=rs.getRecord(2);
     if(b.length==88&&b[0]==2&&(b[87]==42||b[87]==43)){
      if(b[87]==43&&level==1000)finished=1;
      if(b[1]==1){
      int sum=0;for(int i=0;i<85;i++)sum=(sum+(b[i]&255))&65535;
      int stored=((b[85]&255)<<8)|(b[86]&255);
      int lv=((b[2]&255)<<8)|(b[3]&255);
      int m=((b[4]&255)<<8)|(b[5]&255);
      int target=((b[6]&255)<<8)|(b[7]&255);
      int progress=((b[8]&255)<<8)|(b[9]&255);
      int k=b[10]&255,px=b[11]&255,py=b[12]&255;
      boolean valid=sum==stored&&lv==level&&m>0&&m<1000&&target>0&&target<1000
       &&progress<target&&k==(level-1)%5&&px<6&&py<6;
      for(int i=13;i<49;i++)if((b[i]&255)>5)valid=false;
      for(int i=49;i<85;i++)if((b[i]&255)>2)valid=false;
      if(valid){
       moves=m;goal=target;done=progress;kind=k;cx=px;cy=py;
       int p=13;for(int y=0;y<6;y++)for(int x=0;x<6;x++)gems[y][x]=b[p++]&255;
       for(int y=0;y<6;y++)for(int x=0;x<6;x++)blocks[y][x]=b[p++]&255;
       active=1;screen=MENU;notice="";selectionX=-1;selectionY=-1;
      }
      }
     }
    }
   }catch(Exception e){}finally{if(rs!=null)try{rs.closeRecordStore();}catch(Exception e){}}
  }
 }
}
