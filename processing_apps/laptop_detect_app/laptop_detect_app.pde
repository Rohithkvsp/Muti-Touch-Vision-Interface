import vialab.SMT.*;

void setup()
{
 //size(400,400,SMT.RENDERER); 
 size(displayWidth,displayHeight,SMT.RENDERER); 
 SMT.init(this, TouchSource.AUTOMATIC);
  smooth();
}
void draw()
{
  background(150); 
}

boolean sketchFullScreen() {
  return true;
}

