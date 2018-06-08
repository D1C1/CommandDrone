package src;

public class DistCircle {
	
	public double FindDistance(int num, double radius) {
		
		double dist=0;
		double dia;
		dia=radius*2;
		
		switch (num) {
		//(W x F) / P
		case 0: case 4:
			dist = DistCalc(122,dia,942.62);
			break;
		case 2:
			dist = DistCalc(92.5,dia,562.162);
			break;
		case 5: case 6:
			dist = DistCalc(83.5,dia,1053.89);
			break;
		case 7:
			dist = DistCalc(26.5,dia,1132.07);
			break;
		default:
			break;
		}
		
		return dist;
	}
	public double DistCalc(double width, double dia, double focal) {
		double dist;
		dist = (width*focal)/dia;
		
		return dist;
		
	}

}
