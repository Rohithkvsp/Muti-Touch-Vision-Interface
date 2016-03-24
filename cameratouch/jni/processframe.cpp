#include <limits>
#include<jni.h>
#include<vector>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
using namespace std;
using namespace cv;

Size s;
Mat grayscale(s, CV_8UC1);
Mat thresholdImg(s, CV_8UC1);
int min_contour_size=800;//400;
int m_minContourLengthAllowed=200;  //100;

 vector<vector<Point> >Refined_contours;
 vector<vector<Point2f> > possible_rect;
 vector< vector<Point2f> > refined_possible_rect;
 vector< vector<Point2f> > refined_extendPoints;
 vector<Point2f> refined_point_rect;
 //vector<float>scales;

 Scalar color_red(0,0,255);
 Scalar color_blue(255,0,0);
 Scalar color_green(0,255,0);
 Scalar color_yellow(0,255,255);


float perimeter(vector<Point2f> &a);
void convert_grayscale(Mat &img1,Mat &img2);
void find_thresold(Mat &img1,Mat &img2);
void find_contours(Mat &thresholdImg, vector<vector<Point> > &Refined_contours);
void find_rects(vector<vector<Point> > &Refined_contours, vector<vector<Point2f> > &possible_rect);
void refine_rects(vector<vector<Point2f> > &possible_rect,vector<vector<Point2f> > &refined_possible_rect);
void draw_rects(vector<vector<Point2f> > &refined_possible_rect,Mat &img, vector<vector<Point2f> > &refined_extendPoints);
void recognizeLaptop(Mat &img, vector<vector<Point2f> > &refined_possible_rect,vector<vector<Point2f> > &refined_extendPoints,vector<Point2f> &refined_point_rect,Mat &mat_rect);
void refined_rect_to_mat(vector<Point2f> &refined_point_rect, Mat &mat_rect);
int found_laptop(Mat &quad_img);
float Distance(Point2f& p, Point2f& q);

extern "C" {

JNIEXPORT jint JNICALL Java_com_example_cameratouch_Detect_proces(JNIEnv*, jobject, jlong addrRgba, jlong addrGray,jlong mat_rect_Points);


JNIEXPORT jint JNICALL Java_com_example_cameratouch_Detect_process(JNIEnv*, jobject, jlong addrRgba, jlong addrGray,jlong mat_rect_Points) {
	 Mat& mRgb = *(Mat*)addrRgba;
	 Mat& mDect = *(Mat*)addrGray;
	 Mat& mat_rect= *(Mat*)mat_rect_Points;
	 convert_grayscale(mRgb,grayscale);
	 find_thresold(grayscale,thresholdImg);
	 find_contours(thresholdImg,Refined_contours);
	 find_rects(Refined_contours,possible_rect);
	 refine_rects(possible_rect,refined_possible_rect);
	 draw_rects(refined_possible_rect,mRgb,refined_extendPoints);
	 recognizeLaptop(mRgb,refined_possible_rect,refined_extendPoints,refined_point_rect,mat_rect);
	// refined_rect_to_mat(refined_point_rect,mat_rect);
	 mDect =mRgb;
	 return (1);

}
}

void convert_grayscale(Mat &img1,Mat &img2)
{
	cvtColor(img1,img2, CV_BGR2GRAY);
}
void find_thresold(Mat &img1,Mat &img2)
{
	threshold(img1, img2, 127, 255, cv::THRESH_BINARY_INV);
}
void find_contours(Mat &thresholdImg, vector<vector<Point> > &Refined_contours )
{
	vector<Vec4i> hierarchy;
	 vector<vector<Point> > contours;
	findContours(thresholdImg,contours,hierarchy, CV_RETR_TREE,CV_CHAIN_APPROX_NONE);
	 Refined_contours.clear();
	for( int i = 0; i< contours.size(); i++ )
	    {


	       //if(hierarchy[i][0]!=-1)
	         //drawContours(output , contours, i, color_red, 1, 8, hierarchy );

	       //if(hierarchy[i][1]!=-1)
	         //drawContours(output , contours, i, color_blue, 1, 8, hierarchy );

	      // if(hierarchy[i][2]!=-1)
	        // drawContours(output , contours, i, color_green, 1, 8, hierarchy );

	      if(hierarchy[i][3]!=-1)
	      {
	          int contourSize = contours[i].size();
	          if(contourSize>min_contour_size)
	          {
	              Refined_contours.push_back(contours[i]);
	          }
	      }
	    }

}

void find_rects(vector<vector<Point> > &Refined_contours, vector<vector<Point2f> > &possible_rect)
{
	vector<Point2f> refined_points;
	vector<Point>  approxCurve;
	approxCurve.clear();
	possible_rect.clear();
	 for (size_t i=0; i<Refined_contours.size(); i++)
	    {
	         double eps = Refined_contours[i].size() * 0.05;
	        approxPolyDP(Refined_contours[i], approxCurve, eps, true);
	        if (approxCurve.size() != 4)
	            continue;

	        if (!isContourConvex(approxCurve))
	            continue;

	        float minDist = std::numeric_limits<float>::max();
	        for (int i = 0; i < 4; i++)
	        {
	            Point side = approxCurve[i] - approxCurve[(i+1)%4];
	            float squaredSideLength = side.dot(side);
	            minDist = std::min(minDist, squaredSideLength);
	        }
	        if (minDist < m_minContourLengthAllowed)
	            continue;
	        for(int i=0;i<4;i++)
	          refined_points.push_back(Point2f(approxCurve[i].x,approxCurve[i].y));

	        Point v1 = refined_points[1] - refined_points[0];
	        Point v2 = refined_points[2] - refined_points[0];

	        double o = (v1.x * v2.y) - (v1.y * v2.x);
	        //cout<<"Double"<<o<<endl;
	        if (o < 0.0)		 //if the third point is in the left side, then sort in anti-clockwise order
	            std::swap(refined_points[1], refined_points[3]);

	        if(Distance(refined_points[0],refined_points[3])>Distance(refined_points[0],refined_points[1]))
	         {
	               rotate(refined_points.begin(),refined_points.begin()+3,refined_points.end());
	                     //cout<<"distance"<<endl;
	         }
            //scales.push_back(Distance(refined_points[0],refined_points[3])/Distance(refined_points[0],refined_points[4]));
	        possible_rect.push_back(refined_points);
	        refined_points.clear();

	    }
}

void refine_rects(vector<vector<Point2f> > &possible_rect, vector<vector<Point2f> > &refined_possible_rect)
{
	 vector<pair<int,int> > tooNearCandidates;
	    for (size_t i=0;i<possible_rect.size();i++)
	     {

	        for (size_t j=i+1;j<possible_rect.size();j++)
	         {
	             float distSquared = 0;
	             for (int c = 0; c < 4; c++)
	             {
	                 Point v = possible_rect[i][c] -possible_rect[j][c];
	                  distSquared += v.dot(v);
	               }

	                distSquared /= 4;
	                if (distSquared < 100)
	                {
	                 tooNearCandidates.push_back(pair<int,int>(i,j));
	                }

	         }
	     }

	     vector<bool> removalMask (possible_rect.size(), false);
	     for (size_t i=0; i<tooNearCandidates.size(); i++)
	    	     {
	    	         float p1 = perimeter(possible_rect[tooNearCandidates[i].first ]);
	    	         float p2 = perimeter(possible_rect[tooNearCandidates[i].second]);

	    	         size_t removalIndex;
	    	         if (p1 > p2)
	    	             removalIndex = tooNearCandidates[i].second;
	    	         else
 	    	             removalIndex = tooNearCandidates[i].first;

	    	         removalMask[removalIndex] = true;
	    	     }

	        refined_possible_rect.clear();
	    	 for (size_t i=0;i<possible_rect.size();i++)
	    	 {
	    	 if (!removalMask[i])
	    	 {
	    	   refined_possible_rect.push_back(possible_rect[i]);
	    	   //possibleMarkers[i].drawContour(mRgb);
	    	 }

	    	}
}
void draw_rects(vector<vector<Point2f> > &refined_possible_rect,Mat &img, vector<vector<Point2f> > &refined_extendPoints)
{
	   string a;
	   vector<Point2f>extendPoints;
	   refined_extendPoints.clear();
	    for(int i=0;i<refined_possible_rect.size();i++)
	    {

	     float scale=Distance(refined_possible_rect[i][0],refined_possible_rect[i][1])/Distance(refined_possible_rect[i][0],refined_possible_rect[i][3]);
	     float x_scale=35/scale;//35
	     float y_scale=35/scale;//35
	      for(int j=0;j<refined_possible_rect[i].size();j++)
	      {
	      ////  line(img,Point(refined_possible_rect[i][j].x,refined_possible_rect[i][j].y),Point(refined_possible_rect[i][(j+1)%4].x,refined_possible_rect[i][(j+1)%4].y),color_green,1,8);
	        //line(input,Point(possible_rect[i][j].x,possible_rect[i][j].y),Point(possible_rect[i][(j+1)%4].x,possible_rect[i][(j+1)%4].y),color_green,1,8);
           /**
	        if(j==0)
	          a="first";
	        else if(j==1)
	          a="second";
	        else if(j==2)
	          a="third";
	        else if(j==3)
	          a="fourth";
	          **/
	        if(j==0) {
	          a="first";
	          extendPoints.push_back(Point(refined_possible_rect[i][j].x-x_scale,refined_possible_rect[i][j].y-y_scale)); }
	        else if(j==1) {
	          a="second";
	          extendPoints.push_back(Point(refined_possible_rect[i][j].x+x_scale,refined_possible_rect[i][j].y-y_scale)); }
	        else if(j==2) {
	          a="third";
	          extendPoints.push_back(Point(refined_possible_rect[i][j].x+x_scale,refined_possible_rect[i][j].y+y_scale)); }
	        else if(j==3) {
	          a="fourth";
	          extendPoints.push_back(Point(refined_possible_rect[i][j].x-x_scale,refined_possible_rect[i][j].y+y_scale)); }

	         //putText(img,a,refined_possible_rect[i][j],FONT_HERSHEY_SCRIPT_SIMPLEX,2,color_blue,2,8);
	      }

        /**
	      for( int i=0;i<extendPoints.size();i++)
	      {
	       //   cout<<"extended points "<<extendPoints[i].x<<","<<extendPoints[i].y<<endl;
	       line(img,Point(extendPoints[i].x,extendPoints[i].y),Point(extendPoints[(i+1)%4].x,extendPoints[(i+1)%4].y),color_green,1,8);
	       //line(input,Point(extendPoints[i].x,extendPoints[i].y),Point(extendPoints[(i+1)%4].x,extendPoints[(i+1)%4].y),color_green,1,8);

	      }
	      **/
	      refined_extendPoints.push_back(extendPoints);
	      extendPoints.clear();

	    }

}

void recognizeLaptop(Mat &img,vector<vector<Point2f> > &refined_possible_rect,vector<vector<Point2f> > &refined_extendPoints,vector<Point2f> &refined_point_rect,Mat &mat_rect)
{
	refined_point_rect.clear();
		     		            //quad_pts.push_back(Point2f(0, 0));
	for(int i=0;i<refined_extendPoints.size();i++)
    {
		vector<Point2f> quad_pts;
		Mat quad = cv::Mat::zeros(2*100, 2*180, CV_8UC3);

	    quad_pts.push_back(Point2f(0, 0));
	    quad_pts.push_back(Point2f(quad.cols-1, 0));
		quad_pts.push_back(Point2f(quad.cols-1, quad.rows-1));
		quad_pts.push_back(Point2f(0, quad.rows-1));

		Mat transmtx = getPerspectiveTransform(refined_extendPoints[i], quad_pts);
		warpPerspective(img, quad, transmtx, quad.size());
		quad_pts.clear();

		/**
		int w=quad.cols;
	    int h=quad.rows;

	     int x_steps=18*2;
	    int y_steps=10*2;
		int cell_size=10;
		for(size_t x=0;x<=x_steps+1;x++)
		{
		 for(size_t y=0;y<=y_steps+1;y++)
		 {
		   int cell_x=x*cell_size;
		   int cell_y=y*cell_size;
		   line(quad, Point2f(x,y*cell_size), Point2f(x+w-1,y*cell_size), color_blue, 1);///horizontalline
		   line(quad, Point2f(x*cell_size,y), Point2f(x*cell_size,y+h-1),color_blue, 1);///vericalline
	                //cv::Mat cell = grey(cv::Rect(cellX,cellY,cellSize,cellSize))
		  }
		}
	    **/
	    if(found_laptop(quad)==1)//laptopfound
	    {
		  quad.copyTo(img(Rect(quad.cols*i,0,quad.cols,quad.rows)));
		  //Mat rects= Mat(refined_possible_rect);
		  //mat_rect=rects;


		  for(int j=0;j<refined_possible_rect[i].size();j++)
		  {
	      line(img,Point(refined_possible_rect[i][j].x,refined_possible_rect[i][j].y),Point(refined_possible_rect[i][(j+1)%4].x,refined_possible_rect[i][(j+1)%4].y),color_green,1,8);
	      refined_point_rect.push_back(refined_possible_rect[i][j]);
		  }

		  if(refined_possible_rect.size()>0)
		  	{
		  	// Mat rects= Mat(refined_possible_rect);
		  	 //rects.copyTo(mat_rect);
		  	 mat_rect= Mat(refined_possible_rect[0]);

		  	 refined_point_rect.clear();

		  	}
	    }

    }

}

void refined_rect_to_mat(vector<Point2f> &refined_point_rect, Mat &mat_rect)
{
	if(refined_possible_rect.size()>0)
	{
	// Mat rects= Mat(refined_possible_rect);
	 //rects.copyTo(mat_rect);
	 mat_rect= Mat(refined_possible_rect[0]);

	 refined_point_rect.clear();

	}
}

int found_laptop(Mat &quad_img)
{
	int w=quad_img.cols;
    int h=quad_img.rows;
    Mat final_thresold(2*100, 2*177, CV_8UC3);
    cvtColor(quad_img,final_thresold,CV_BGRA2GRAY);
    threshold(final_thresold,final_thresold , 70, 255, CV_THRESH_BINARY_INV);

    Mat subMat1=final_thresold(Rect(0,0,w,10));///top
    int count_nonzero_top=countNonZero(subMat1);
    int total_nonzero_top=w*10;

    Mat subMat2=final_thresold(Rect(0,10,10,h-10));///left
    int count_nonzero_left=countNonZero(subMat2);
    int total_nonzero_left=10*h-10;

     Mat subMat3=final_thresold(Rect(w-10,10,10,h-10));///right
     int count_nonzero_right=countNonZero(subMat3);
     int total_nonzero_right=10*h-10;

     Mat subMat4=final_thresold(Rect(0,h-10,w,10));///bottom
     int count_nonzero_bottom=countNonZero(subMat4);
     int total_nonzero_bottom=w*10;

     if((count_nonzero_top>total_nonzero_top/2)&&
        (count_nonzero_left>total_nonzero_left/2)&&
        (count_nonzero_right>total_nonzero_right/2)&&
        (count_nonzero_bottom>total_nonzero_bottom/2))
        {
    	 return 1;
        }
     else
     {
    	return -1;
     }
}


float perimeter(vector<Point2f> &a)
{

	  float sum=0, dx, dy;

	    for (size_t i=0;i<a.size();i++)
	    {
	      size_t i2=(i+1) % a.size();

	      dx = a[i].x - a[i2].x;
	      dy = a[i].y - a[i2].y;

	      sum += sqrt(dx*dx + dy*dy);
	    }

	    return sum;
}

float Distance(Point2f& p, Point2f& q) {
    Point diff = p - q;
    return cv::sqrt(diff.x*diff.x + diff.y*diff.y);
}
