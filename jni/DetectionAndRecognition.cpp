#include "DetectionAndRecognition.h"
#include <opencv2/core/core.hpp>
#include <opencv2/contrib/detection_based_tracker.hpp>

#include <string>
#include <vector>

#include <android/log.h>
#include "fisherfaces.h"
#include "lbph.h"
#include "fisherfaces.cpp"
#include "lbph.cpp"

#define LOG_TAG "FaceDetectionAndRecognition"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;


inline void vector_Rect_to_Mat(vector<Rect>& v_rect, Mat& mat)
{
    mat = Mat(v_rect, true);
}

void saveImage(Mat image, const char * path){
	IplImage img = image;
	int res = 0;//cvSaveImage("/sdcard/selcuk.pgm", &img);
	bool written = imwrite(path, image);
	LOGD("------------------------------------------- saved: %d written: %d", res, written);
}


JNIEXPORT jlong JNICALL Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeCreateObject
(JNIEnv * jenv, jclass, jstring jFileName, jint faceSize, jboolean willDetectFaces)
{
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeCreateObject enter");
    const char* jnamestr = jenv->GetStringUTFChars(jFileName, NULL);
    string stdFileName(jnamestr);
    jlong result = 0;

    try
    {
        DetectionBasedTracker::Parameters DetectorParams;
        if (faceSize > 0)
            DetectorParams.minObjectSize = faceSize;
        if(!willDetectFaces){
        	DetectorParams.maxObjectSize = 500;
        	DetectorParams.scaleFactor = 1.1;
        	DetectorParams.minNeighbors = 3;

        }
        result = (jlong)new DetectionBasedTracker(stdFileName, DetectorParams);
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeCreateObject caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
        return 0;
    }

    LOGD("Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeCreateObject exit");
    return result;
}

JNIEXPORT void JNICALL Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeDestroyObject
(JNIEnv * jenv, jclass, jlong thiz)
{
    LOGD("Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeDestroyObject enter");
    try
    {
        if(thiz != 0)
        {
            ((DetectionBasedTracker*)thiz)->stop();
            delete (DetectionBasedTracker*)thiz;
        }
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeestroyObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeDestroyObject caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
    }
    LOGD("Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeDestroyObject exit");
}

JNIEXPORT void JNICALL Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeStart
(JNIEnv * jenv, jclass, jlong thiz)
{
    LOGD("Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeStart enter");
    try
    {
        ((DetectionBasedTracker*)thiz)->run();
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeStart caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeStart caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
    }
    LOGD("Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeStart exit");
}

JNIEXPORT void JNICALL Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeStop
(JNIEnv * jenv, jclass, jlong thiz)
{
    LOGD("Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeStop enter");
    try
    {
        ((DetectionBasedTracker*)thiz)->stop();
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeStop caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeStop caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
    }
    LOGD("Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeStop exit");
}

JNIEXPORT void JNICALL Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeSetFaceSize
(JNIEnv * jenv, jclass, jlong thiz, jint faceSize)
{
    LOGD("Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeSetFaceSize enter");
    try
    {
        if (faceSize > 0)
        {
            DetectionBasedTracker::Parameters DetectorParams = \
            ((DetectionBasedTracker*)thiz)->getParameters();
            DetectorParams.minObjectSize = faceSize;
            ((DetectionBasedTracker*)thiz)->setParameters(DetectorParams);
        }
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeStop caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeSetFaceSize caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
    }
    LOGD("Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeSetFaceSize exit");
}


JNIEXPORT void JNICALL Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeDetect
(JNIEnv * jenv, jclass, jlong thiz, jlong imageGray, jlong faces)
{
    //LOGD("Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeDetect enter");
    try
    {
        vector<Rect> RectFaces;
        ((DetectionBasedTracker*)thiz)->process(*((Mat*)imageGray));
        ((DetectionBasedTracker*)thiz)->getObjects(RectFaces);
        vector_Rect_to_Mat(RectFaces, *((Mat*)faces));
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeDetect caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
    }
    LOGD("Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeDetect exit");
}

JNIEXPORT jint JNICALL Java_com_yaylas_sytech_facerecognizer_FaceRecognitionActivity_faceRecognition
(JNIEnv * jenv, jclass jclazz, jlong sample, jint size)
{


	jniEnv = jenv;
    LOGD("Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_faceRecognition enter");
    try
    {
    	jclass clazz = jenv->FindClass("com/itu/yaylas/facerecognizer/FaceRecognitionActivity");
    	jmethodID getFaceFolder = jenv->GetStaticMethodID(clazz, "getFaceFolder","(I)Ljava/lang/String;");
    	jmethodID getPersonID = jenv->GetStaticMethodID(clazz, "getPersonID","(I)J");

    	vector<Mat> images;
    	vector<int> labels;
    	for(int i = 0; i<size; i++){
    		jstring folderName = (jstring)jenv->CallStaticObjectMethod(clazz, getFaceFolder, (jint)i);
    		jboolean isCopy;
    		const char *savePath = jenv->GetStringUTFChars(folderName, &isCopy);
    		jlong id = jenv->CallStaticLongMethod(clazz, getPersonID, (jint)i);
    		std::ostringstream stringstream;
    		for(int j = 0; j < 10; j++) {
    			stringstream << savePath;
    			stringstream << "/";
    			stringstream << j;
    			stringstream << ".jpg";
    			Mat greymat, colormat;
    			colormat = imread(stringstream.str().c_str());
    			if(colormat.data) {
    				LOGD("---------------------------------------------- image Loaded");
					cvtColor(colormat, greymat, CV_BGR2GRAY);
					images.push_back(greymat);
					labels.push_back((int)id);
    			}
    			stringstream.clear();

    		}
    	}
    	vector<Mat> histograms;
    	jint lbpResult = lbpRecognize(images, labels, *((Mat*)sample));

    	/*jint result = fisherRecognize(images, labels, *((Mat*)sample));
    	if(result != lbpResult){
    		result = -2;
    	}*/
    	return lbpResult;

    }
    catch(cv::Exception& e)
    {
        LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
        return -1;
    }
    catch (...)
    {
        LOGD("nativeDetect caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
        return - 1;
    }
    LOGD("Java_com_yaylas_sytech_facerecognizer_DetectionBasedTracker_nativeDetect exit");
        return - 1;
}
JNIEXPORT void JNICALL Java_com_yaylas_sytech_facerecognizer_utils_ImageUtils_saveImageAsPGM
(JNIEnv * jenv, jclass jclazz, jstring path, jlong imageReference)
{
	jboolean isCopy;
	const char *savePath = jenv->GetStringUTFChars(path, &isCopy);
	Mat image = *((Mat*)imageReference);
	saveImage(image, savePath);


}

