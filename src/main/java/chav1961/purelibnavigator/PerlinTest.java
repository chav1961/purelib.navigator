package chav1961.purelibnavigator;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class PerlinTest {
	private static final int NUM_OCTAVES = 16;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final int			width = 128, height = 128;
		final BufferedImage	bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		final float 		fac = (float) Math.random();//random(Math.PI*2*10,Math.PI*3*10);

		for(int x=0 ; x<width; x++) {
			for(int y=0 ; y<height; y++) {
		       //проходим по всем элементам массива и заполняем их значениями   
				bi.setRGB(x, y, ((1 << 16) + (1 << 8) + 1) * perlinNoise_2D(x, y, fac));
			}
		}
		
		final JLabel		label = new JLabel(new ImageIcon(bi));
		
		JOptionPane.showMessageDialog(null, label);
	}

	private static float noise2D(int x, int y) {
	  int n = x + y * 57;
	  
	  n = (n<<13) ^ n;
	  
	  return 1.0f - ( (n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0f;
	}
	
	private static float smoothedNoise2D(int x, int y) { 
	    float corners = ( noise2D(x-1, y-1)+noise2D(x+1, y-1)+ noise2D(x-1, y+1)+noise2D(x+1, y+1) ) / 16f;
	    float sides   = ( noise2D(x-1, y)  +noise2D(x+1, y)  + noise2D(x, y-1)  +noise2D(x, y+1) ) /  8f;
	    float center  =  noise2D(x, y) / 4f;
	    return corners + sides + center;
	}
	
	private static float cosine_Interpolate(float a , float b , float x) {
		float ft = (float) (x * Math.PI);
		float f = (float) ((1 - Math.cos(ft)) * 0.5);
		return a*(1-f) + b*f; 	
	}
	
	private static float pow_Interpolate(float a , float b , float x) {
	  float fac1 = (float) (3*Math.pow(1-a, 2) - 2*Math.pow(1-a,3));
	  float fac2 = (float) (3*Math.pow(a, 2) - 2*Math.pow(a, 3));
	  return a*fac1 + b*fac2;// James Long- использовал этот метод в своей статье
	}

	private static float some_Interpolate(float a , float b , float x) {
		return pow_Interpolate(a, b, x);
	}
	
	private static float compileNoise(float x, float y) {
	      int int_X = (int)x;//целая часть х
	      float fractional_X = x - int_X;//дробь от х
	//аналогично у
	      int int_Y = (int)y;
	      float fractional_Y = y - int_Y;
	  //получаем 4 сглаженных значения
	     float v1 = smoothedNoise2D(int_X,     int_Y);
	     float v2 = smoothedNoise2D(int_X + 1, int_Y);
	     float v3 = smoothedNoise2D(int_X,     int_Y + 1);
	     float v4 = smoothedNoise2D(int_X + 1, int_Y + 1);
	  //интерполируем значения 1 и 2 пары и производим интерполяцию между ними
	      float i1 = some_Interpolate(v1 , v2 , fractional_X);
	      float i2 = some_Interpolate(v3 , v4 , fractional_X);
	  //я использовал косинусною интерполяцию ИМХО лучше 
	  //по параметрам быстрота-//качество
	      return some_Interpolate(i1 , i2 , fractional_Y);
	}
	
	private static int perlinNoise_2D(float x, float y, final float factor) {
	   float total = 0;
	   // это число может иметь и другие значения хоть cosf(sqrtf(2))*3.14f 
	   // главное чтобы было красиво и результат вас устраивал
	   float persistence=0.5f;

	   // экспериментируйте с этими значениями, попробуйте ставить 
	   // например sqrtf(3.14f)*0.25f или что-то потяжелее для понимания J)
	   float frequency = 0.25f;
	   float amplitude=1;//амплитуда, в прямой зависимости от значения настойчивости

	   // вводим фактор случайности, чтобы облака не были всегда одинаковыми
	   // (Мы ведь помним что ф-ция шума когерентна?) 
	    
	   x += factor;
	   y += factor;

	   // NUM_OCTAVES - переменная, которая обозначает число октав,
	   // чем больше октав, тем лучше получается шум
	   for(int i=0; i < NUM_OCTAVES; i++) {
	       total += compileNoise(x*frequency, y*frequency) * amplitude;
	       amplitude *= persistence;
	       frequency *=2;
	    }
	    //здесь можно перевести значения цвета   по какой-то формуле
	    //например:
	    //total=sqrt(total);
	    // total=total*total;
	    // total=sqrt(1.0f/float(total)); 
	    //total=255-total;-и.т.д все зависит от желаемого результата
	    total= Math.abs(total);
	    int res=(int)(total*255.0f);//приводим цвет к значению 0-255…
	    return res;
	}

	
}
