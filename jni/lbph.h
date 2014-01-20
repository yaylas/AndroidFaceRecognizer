#include "opencv2/core/core.hpp"
using namespace std;
using namespace cv;
int lbpRecognize(vector<Mat> images, vector<int> labels, Mat testSample);

#define LOG_TAG "FaceDetectionAndRecognition"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
