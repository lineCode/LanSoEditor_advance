package com.lansosdk.videoeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.lansosdk.box.AudioSource;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.CanvasLayer;
import com.lansosdk.box.DataLayer;
import com.lansosdk.box.DrawPadVideoRunnable;
import com.lansosdk.box.GifLayer;
import com.lansosdk.box.LSLog;
import com.lansosdk.box.Layer;
import com.lansosdk.box.MVLayer;
import com.lansosdk.box.TimeRange;
import com.lansosdk.box.VideoLayer;
import com.lansosdk.box.onAudioPadThreadProgressListener;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadErrorListener;
import com.lansosdk.box.onDrawPadOutFrameListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadThreadProgressListener;

import java.util.ArrayList;
import java.util.List;

import com.lansosdk.LanSongFilter.LanSongFilter;

public class DrawPadVideoExecute {

    private static final String TAG = LSLog.TAG;
    protected boolean isCheckBitRate = true;
    protected boolean isCheckPadSize = true;
    private DrawPadVideoRunnable renderer = null;
    private int padWidth, padHeight;
    private boolean mPauseRecord = false;
    public MediaInfo mediaInfo;


    /**
     * 构造方法
     * @param ctx
     * @param srcPath
     * @param dstPath  <----注意:这里设置目标文件字符串;
     */
    public DrawPadVideoExecute(Context ctx, String srcPath, String dstPath) {
        this.init(ctx,srcPath,0,dstPath);
    }
    /**
     * 设置容器的宽高
     * [可选,不建议使用]
     * 设置后, 视频的宽高会缩放到容器中;
     * 容器的宽高, 就是录制后的视频的宽高;
     * @param width
     * @param height
     */
    public void setDrawPadSize(int width,int height){
        if( renderer!=null && width>0 && height>0){
            renderer.setScaleValue(width,height);
            this.padWidth = width;
            this.padHeight = height;
        }
    }
    /**
     * 设置编码码率
     * @param bitrate
     */
    public void setRecordBitrate(int bitrate){
        if(bitrate>0 &&renderer!=null){
            renderer.setEncoderBitrate(bitrate);
        }
    }

    /**
     * 设置开始时间;
     * @param timeUs
     */
    public void setStartTimeUs(long timeUs){
        if(timeUs>0 &&renderer!=null){
            renderer.setStartTimeUs(timeUs);
        }
    }

    /**
     * 设置处理时长.
     * (注意:不是结束时间点, 是一个时长)
     * @param timeUs
     */
    public void setDurationTimeUs(long timeUs){
        if(timeUs>0 &&renderer!=null){
            renderer.setDurationTimeUs(timeUs);
        }
    }
    /**
     * 给视频设置一个全局滤镜;
     * 注意:滤镜应该是新创建的;
     * @param filter
     */
    public void setVideoFilter(LanSongFilter filter){
        if(filter!=null &&renderer!=null){
            renderer.setVideoFilter(filter);
        }
    }

    @Deprecated
    public DrawPadVideoExecute(Context ctx, String srcPath, long startTimeUs, String dstPath) {
        this.init(ctx,srcPath,startTimeUs,dstPath);
    }
    @Deprecated
    public DrawPadVideoExecute(Context ctx, String srcPath, int padwidth,
                               int padheight, int bitrate, LanSongFilter filter, String dstPath) {
        if (renderer == null) {
            renderer = new DrawPadVideoRunnable(ctx, srcPath, padwidth,
                    padheight, bitrate, filter, dstPath);
        }
        this.padWidth = padwidth;
        this.padHeight = padheight;
    }
    @Deprecated
    public DrawPadVideoExecute(Context ctx, String srcPath, int padwidth,
                               int padheight, LanSongFilter filter, String dstPath) {
        if (renderer == null) {
            renderer = new DrawPadVideoRunnable(ctx, srcPath, padwidth,
                    padheight, 0, filter, dstPath);
        }
        this.padWidth = padwidth;
        this.padHeight = padheight;
    }
    @Deprecated
    public DrawPadVideoExecute(Context ctx, String srcPath, long startTimeUs,
                               int padwidth, int padheight, int bitrate, LanSongFilter filter,
                               String dstPath) {
        if (renderer == null) {
            renderer = new DrawPadVideoRunnable(ctx, srcPath, startTimeUs,
                    padwidth, padheight, bitrate, filter, dstPath);
        }
        this.padWidth = padwidth;
        this.padHeight = padheight;
    }
    @Deprecated
    public DrawPadVideoExecute(Context ctx, String srcPath, long startTimeUs,
                               int padwidth, int padheight, LanSongFilter filter, String dstPath) {
        if (renderer == null) {
            renderer = new DrawPadVideoRunnable(ctx, srcPath, startTimeUs,
                    padwidth, padheight, 0, filter, dstPath);
        }
        this.padWidth = padwidth;
        this.padHeight = padheight;
    }


    private void init(Context ctx, String srcPath, long startTimeUs,String dstPath)
    {
        mediaInfo=new MediaInfo(srcPath);
        if (renderer == null && mediaInfo.prepare()) {
            int padW=mediaInfo.getWidth();
            int padH=mediaInfo.getHeight();

            int bitrate=VideoEditor.getSuggestBitRate(padH *padW);
            renderer = new DrawPadVideoRunnable(ctx, srcPath, startTimeUs, padW,padH,bitrate, null, dstPath);
            this.padWidth = padW;
            this.padHeight = padH;
        }
    }
    public void setLanSongVideoMode(boolean is) {
        if (renderer != null) {
            renderer.setEditModeVideo(is);
        }
    }

    /**
     * 启动DrawPad,开始执行.
     * <p>
     * 开启成功,返回true, 失败返回false
     */
    public boolean startDrawPad() {
        if (renderer != null && renderer.isRunning() == false) {
            return renderer.startDrawPad();
        }
        return false;
    }

    public void stopDrawPad() {
        if (renderer != null && renderer.isRunning()) {
            renderer.stopDrawPad();
        }
    }
    /**
     * 音频处理的进度, 如果你要调整每个AudioSource对象在不同时段内的各种状态,则可以在这个进度中, 判断时间戳来调整.
     * 比如静音,比如增大音量
     *
     * @param li
     */
    public void setAudioProgressListener(onAudioPadThreadProgressListener li) {
        if (renderer != null) {
            renderer.setAudioProgressListener(li);
        }
    }
    /**
     * DrawPad
     * 每执行完一帧画面,会调用这个Listener,返回的timeUs是当前画面的时间戳(微妙),
     * 可以利用这个时间戳来做一些变化,比如在几秒处缩放, 在几秒处平移等等.从而实现一些动画效果.
     *
     * (注意, 这个进度回调, 是经过Handler异步调用, 工作在主线程的. 如果你要严格按照时间来,则需要用setDrawPadThreadProgressListener)
     */
    public void setDrawPadProgressListener(onDrawPadProgressListener listener) {
        if (renderer != null) {
            renderer.setDrawPadProgressListener(listener);
        }
    }

    /**
     * 方法与 onDrawPadProgressListener不同的地方在于: 即将开始一帧渲染的时候,
     * 直接执行这个回调中的代码,不通过Handler传递出去,你可以精确的执行一些这一帧的如何操作. 故不能在回调 内增加各种UI相关的代码.
     */
    public void setDrawPadThreadProgressListener(
            onDrawPadThreadProgressListener listener) {
        if (renderer != null) {
            renderer.setDrawPadThreadProgressListener(listener);
        }
    }

    /**
     * DrawPad执行完成后的回调.
     *
     * @param listener
     */
    public void setDrawPadCompletedListener(onDrawPadCompletedListener listener) {
        if (renderer != null) {
            renderer.setDrawPadCompletedListener(listener);
        }
    }

    /**
     * 设置当前DrawPad运行错误的回调监听.
     *
     * @param listener
     */
    public void setDrawPadErrorListener(onDrawPadErrorListener listener) {
        if (renderer != null) {
            renderer.setDrawPadErrorListener(listener);
        }
    }

    /**
     * 设置每处理一帧的数据监听, 等于把当前处理的这一帧的画面拉出来, 您可以根据这个画面来自行的编码保存, 或网络传输.
     * <p>
     * 建议在这里拿到数据后, 放到queue中, 然后在其他线程中来异步读取queue中的数据, 请注意queue中数据的总大小, 要及时处理和释放,
     * 以免内存过大,造成OOM问题
     *
     * @param listener 监听对象
     */
    public void setDrawPadOutFrameListener(onDrawPadOutFrameListener listener) {
        if (renderer != null) {
            renderer.setDrawpadOutFrameListener(padWidth, padHeight, 1,
                    listener);
        }
    }

    public void setDrawPadOutFrameListener(int width, int height,
                                           onDrawPadOutFrameListener listener) {
        if (renderer != null) {
            renderer.setDrawpadOutFrameListener(width, height, 1, listener);
        }
    }

    /**
     * 设置setOnDrawPadOutFrameListener后, 你可以设置这个方法来让listener是否运行在Drawpad线程中.
     * 如果你要直接使用里面的数据, 则不用设置, 如果你要开启另一个线程, 把listener传递过来的数据送过去,则建议设置为true;
     *
     * @param en
     */
    public void setOutFrameInDrawPad(boolean en) {
        if (renderer != null) {
            renderer.setOutFrameInDrawPad(en);
        }
    }

    /**
     * 把当前图层放到DrawPad的最底部. DrawPad运行后,有效.
     * @param layer
     */
    public void bringToBack(Layer layer) {
        if (renderer != null && renderer.isRunning()) {
            renderer.bringToBack(layer);
        }
    }

    /**
     * 把当前图层放到最顶层
     *
     * @param layer
     */
    public void bringToFront(Layer layer) {
        if (renderer != null && renderer.isRunning()) {
            renderer.bringToFront(layer);
        }
    }

    /**
     * 改变指定图层的位置.
     *
     * @param layer
     * @param position
     */
    public void changeLayerPosition(Layer layer, int position) {
        if (renderer != null && renderer.isRunning()) {
            renderer.changeLayerPosition(layer, position);
        }
    }

    /**
     * 交换两个图层的位置.
     *
     * @param first
     * @param second
     */
    public void swapTwoLayerPosition(Layer first, Layer second) {
        if (renderer != null && renderer.isRunning()) {
            renderer.swapTwoLayerPosition(first, second);
        }
    }

    /**
     * 获取当前容器中有多少个图层.
     *
     * @return
     */
    public int getLayerSize() {
        if (renderer != null) {
            return renderer.getLayerSize();
        } else {
            return 0;
        }
    }

    /**
     * 得到当前DrawPadVideoRunnable中设置的视频图层对象.
     *
     * @return
     */
    public VideoLayer getMainVideoLayer() {
        if (renderer != null && renderer.isRunning()) {
            return renderer.getMainVideoLayer();
        } else {
            return null;
        }
    }

    public AudioSource getMainAudioSource() {
        if (renderer != null) {
            return renderer.getMainAudioSource();
        } else {
            return null;
        }
    }
    /**
     * 增加音频, 在DrawPad线程开始前增加;
     *
     * @param srcPath
     * @return
     */
    public AudioSource addSubAudio(String srcPath) {
        if (renderer != null && renderer.isRunning() == false) {
            return renderer.addSubAudio(srcPath);
        } else {
            return null;
        }
    }

    /**
     * 增加其他声音; 在DrawPad线程开始前增加;
     *
     * @param srcPath
     * @param startFromPadTimeUs 从主音频的什么时间开始增加
     * @return
     */
    public AudioSource addSubAudio(String srcPath, long startFromPadTimeUs) {
        if (renderer != null && renderer.isRunning() == false) {
            return renderer.addSubAudio(srcPath, startFromPadTimeUs, -1);
        } else {
            return null;
        }
    }

    /**
     * 增加其他声音;
     * <p>
     * 在DrawPad线程开始前增加;
     *
     * @param srcPath        路径, 可以是mp3或m4a或 带有音频的MP4文件;
     * @param startFromPadUs 从主音频的什么时间开始增加
     * @param durationUs     把这段声音多长插入进去.
     * @return 返回一个AudioSource对象;
     */
    public AudioSource addSubAudio(String srcPath, long startFromPadUs,
                                   long durationUs) {
        if (renderer != null && renderer.isRunning() == false) {
            return renderer.addSubAudio(srcPath, startFromPadUs, durationUs);
        } else {
            return null;
        }
    }

    /**
     * 如果要调节音量, 则增加拿到对象后, 开始调节.
     * 在DrawPad线程开始前增加;
     *
     * @param srcPath
     * @param startFromPadUs   从容器的什么位置开始增加
     * @param startAudioTimeUs 把当前声音的开始时间增加进去.
     * @param durationUs       增加多少, 时长.
     * @return
     */
    public AudioSource addSubAudio(String srcPath, long startFromPadUs,
                                   long startAudioTimeUs, long durationUs) {
        if (renderer != null && renderer.isRunning() == false) {
            return renderer.addSubAudio(srcPath, startFromPadUs,
                    startAudioTimeUs, durationUs);
        } else {
            return null;
        }
    }

    /**
     * 增加时间冻结,即在视频的什么时间段开始冻结, 静止的结束时间; 为了统一: 这里用结束时间; 比如你要从原视频的5秒地方开始静止, 静止3秒钟,
     * 则这里是3*1000*1000 , 8*1000*1000 (画面停止的过程中, 可以做一些缩放,移动等特写等)
     *
     * @param startTimeUs 从输入的视频/音频的哪个时间点开始冻结,
     * @param endTimeUs   (这里理解为:冻结的时长+开始时间);
     */
    public void addTimeFreeze(long startTimeUs, long endTimeUs) {
        if (renderer != null && renderer.isRunning() == false) {
            renderer.addTimeFreeze(startTimeUs, endTimeUs);
        } else {
            Log.e(TAG, "addTimeFreeze error, maybe drawpad is running");
        }
    }

    /**
     * 给这个主视频的音频部分和视频部分,分别做时间拉伸(某一段的速度调节)
     * <p>
     * 这个设置等于分别给当前视频的 VideoLayer和AudioSource分别设置 时间拉伸;
     * <p>
     * 可以被多次调用.
     * <p>
     * 在DrawPad容器开始前调用
     *
     * @param rate        拉伸的速度, 范围0.5--2.0; 0.5是放慢1倍, 2.0是加快一倍; 1.0f是默认,
     *                    没有设置的时间段,默认是1.0f;
     * @param startTimeUs
     * @param endTimeUs
     */
    public void addTimeStretch(float rate, long startTimeUs, long endTimeUs) {
        if (renderer != null && renderer.isRunning() == false) {
            renderer.addTimeStretch(rate, startTimeUs, endTimeUs);
        } else {
            Log.e(TAG, "addTimeStretch error, maybe drawpad is running");
        }
    }

    /**
     * 增加时间重复;
     * <p>
     * 类似综艺节目中, 当好玩的画面发生的时候, 多次重复的效果.
     * <p>
     * 在DrawPad容器开始前调用
     *
     * @param startUs 相对原视频/原音频的开始时间;
     * @param endUs   相对原视频/原音频的结束时间;
     * @param loopcnt 重复的次数;
     */
    public void addTimeRepeat(long startUs, long endUs, int loopcnt) {
        if (renderer != null && renderer.isRunning() == false) {
            renderer.addTimeRepeat(startUs, endUs, loopcnt);
        } else {
            Log.e(TAG, "addTimeRepeat error, maybe drawpad is running");
        }
    }

    /**
     * 增加时间拉伸
     * <p>
     * 在DrawPad容器开始前调用
     *
     * @param list
     */
    public void addTimeStretch(List<TimeRange> list) {
        if (renderer != null && renderer.isRunning() == false) {
            renderer.addTimeStretch(list);
        } else {
            Log.e(TAG, "addTimeStretch error, maybe drawpad is running");
        }
    }

    /**
     * 时间冻结
     * 在DrawPad容器开始前调用
     *
     * @param list
     */
    public void addTimeFreeze(List<TimeRange> list) {
        if (renderer != null && renderer.isRunning() == false) {
            renderer.addTimeFreeze(list);
        } else {
            Log.e(TAG, "addTimeFreeze error, maybe drawpad is running");
        }
    }

    /**
     * 时间重复.
     * 在DrawPad容器开始前调用
     *
     * @param list
     */
    public void addTimeRepeat(List<TimeRange> list) {
        if (renderer != null && renderer.isRunning() == false) {
            renderer.addTimeRepeat(list);
        } else {
            Log.e(TAG, "addTimeRepeat error, maybe drawpad is running");
        }
    }

    /**
     * 增加图片图层.
     * 在DrawPad容器开始后调用;
     *
     * @param bmp
     * @return
     */
    public BitmapLayer addBitmapLayer(Bitmap bmp) {
        if (renderer != null && renderer.isRunning()) {
            return renderer.addBitmapLayer(bmp, null);
        } else {
            return null;
        }
    }

    /**
     * 增加数据图层, DataLayer有一个
     * {@link DataLayer#pushFrameToTexture(java.nio.IntBuffer)}
     * 可以把数据或图片传递到DrawPad中.
     *
     * @param dataWidth
     * @param dataHeight
     * @return
     */
    public DataLayer addDataLayer(int dataWidth, int dataHeight) {
        if (renderer != null && renderer.isRunning()) {
            return renderer.addDataLayer(dataWidth, dataHeight);
        } else {
            return null;
        }
    }

    /**
     * 向DrawPad容器里增加另一个视频, 增加后,等于叠加在原有的视频上面; 这里叠加后, 当前的视频是不透明的;如果你要增加透明视频,则可以用
     * TwoVideoLayer 或MVLayer;
     *
     * @param videoPath  视频的完整路径;
     * @param filter 视频滤镜 ,如果不增加滤镜,则赋值为null
     * @return
     */
    public VideoLayer addVideoLayer(String videoPath, LanSongFilter filter) {
        if (renderer != null && renderer.isRunning()) {
            return renderer.addVideoLayer2(videoPath, filter);
        } else {
            return null;
        }
    }

    /**
     * 当mv在解码的时候, 是否异步执行; 如果异步执行,则MV解码可能没有那么快,从而MV画面会有慢动作的现象.
     * 如果同步执行,则视频处理会等待MV解码完成, 从而处理速度会慢一些,但MV在播放时,是正常的.
     *
     * @param srcPath  MV的彩色视频
     * @param maskPath MV的黑白视频.
     * @param isAsync  是否异步执行.
     * @return
     */
    public MVLayer addMVLayer(String srcPath, String maskPath, boolean isAsync) {
        if (renderer != null && renderer.isRunning()) {
            return renderer.addMVLayer(srcPath, maskPath);
        } else {
            return null;
        }
    }

    /**
     * 增加一个MV图层.
     *
     * @param srcPath
     * @param maskPath
     * @return
     */
    public MVLayer addMVLayer(String srcPath, String maskPath) {
        if (renderer != null && renderer.isRunning()) {
            return renderer.addMVLayer(srcPath, maskPath);
        } else {
            return null;
        }
    }

    /**
     * 增加gif图层
     *
     * @param gifPath
     * @return
     */
    public GifLayer addGifLayer(String gifPath) {
        if (renderer != null && renderer.isRunning()) {
            return renderer.addGifLayer(gifPath);
        } else {
            return null;
        }
    }

    /**
     * 增加gif图层 resId 来自apk中drawable文件夹下的各种资源文件, 我们会在GifLayer中拷贝这个资源到默认文件夹下面,
     * 然后作为一个普通的gif文件来做处理,使用完后, 会在Giflayer 图层释放的时候, 删除.
     *
     * @param resId
     * @return
     */
    public GifLayer addGifLayer(int resId) {
        if (renderer != null && renderer.isRunning()) {
            return renderer.addGifLayer(resId);
        } else {
            return null;
        }
    }

    /**
     * 增加一个Canvas图层, 可以用Android系统的Canvas来绘制一些文字线条,颜色等. 可参考我们的的 "花心形"的举例
     * 因为Android的View机制是无法在非UI线程中使用View的. 但可以使用Canvas这个类工作在其他线程.
     * 因此我们设计了CanvasLayer,从而可以用Canvas来做各种Draw文字, 线条,图案等.
     *
     * @return
     */
    public CanvasLayer addCanvasLayer() {
        if (renderer != null && renderer.isRunning()) {
            return renderer.addCanvasLayer();
        } else {
            return null;
        }
    }

    /**
     * 删除一个图层.
     *
     * @param layer
     */
    public void removeLayer(Layer layer) {
        if (renderer != null && renderer.isRunning()) {
            renderer.removeLayer(layer);
        }
    }

    /**
     * 已废弃.请用pauseRecord();
     */
    @Deprecated
    public void pauseRecordDrawPad() {
        pauseRecord();
    }

    /**
     * 已废弃,请用resumeRecord();
     */
    @Deprecated
    public void resumeRecordDrawPad() {
        resumeRecord();
    }

    /**
     * 暂停录制, 使用在 : 开始DrawPad后, 需要暂停录制, 来增加一些图层, 然后恢复录制的场合. 此方法使用在DrawPad线程中的
     * 暂停和恢复的作用, 不能用在一个Activity的onPause和onResume中.
     */
    public void pauseRecord() {
        if (renderer != null && renderer.isRunning()) {
            renderer.pauseRecordDrawPad();
        } else {
            mPauseRecord = true;
        }
    }

    /**
     * 恢复录制. 此方法使用在DrawPad线程中的 暂停和恢复的作用, 不能用在一个Activity的onPause和onResume中.
     */
    public void resumeRecord() {
        if (renderer != null && renderer.isRunning()) {
            renderer.resumeRecordDrawPad();
        } else {
            mPauseRecord = false;
        }
    }

    /**
     * 是否在录制.
     *
     * @return
     */
    public boolean isRecording() {
        if (renderer != null && renderer.isRunning()) {
            return renderer.isRecording();
        } else {
            return false;
        }
    }

    /**
     * DrawPad是否在运行
     *
     * @return
     */
    public boolean isRunning() {
        if (renderer != null) {
            return renderer.isRunning();
        } else {
            return false;
        }
    }

    @Deprecated
    public void switchFilterTo(Layer layer, LanSongFilter filter) {
        if (renderer != null && renderer.isRunning()) {
            renderer.switchFilterTo(layer, filter);
        }
    }

    @Deprecated
    public void switchFilterList(Layer layer, ArrayList<LanSongFilter> filters) {
        if (renderer != null && renderer.isRunning()) {
            renderer.switchFilterList(layer, filters);
        }
    }

    /**
     * 释放DrawPad,方法等同于 {@link #stopDrawPad()} 只是为了代码标准化而做.
     */
    public void releaseDrawPad() {
        // TODO Auto-generated method stub
        if (renderer != null && renderer.isRunning()) {
            renderer.releaseDrawPad();
        }
        mPauseRecord = false;
        renderer = null;
    }

    /**
     * 停止DrawPad, 并释放资源.如果想再次开始,需要重新new, 然后start.
     * <p>
     * 注意:这里阻塞执行, 只有等待opengl线程执行退出完成后,方返回. 方法等同于 {@link #stopDrawPad()}
     * 只是为了代码标准化而做.
     */
    public void release() {
        releaseDrawPad();
    }

    /**
     * 是否在开始运行DrawPad的时候,检查您设置的码率和分辨率是否正常.
     * <p>
     * 默认是检查, 如果您清楚码率大小的设置,请调用此方法,不再检查.
     */
    public void setNotCheckBitRate() {
        if (renderer != null && renderer.isRunning() == false) {
            renderer.setNotCheckBitRate();
        } else {
            isCheckBitRate = false;
        }
    }

    /**
     * 是否在开始运行DrawPad的时候, 检查您设置的DrawPad宽高是否是16的倍数. 默认是检查.
     */
    public void setNotCheckDrawPadSize() {
        if (renderer != null && renderer.isRunning() == false) {
            renderer.setNotCheckDrawPadSize();
        } else {
            isCheckPadSize = false;
        }
    }

    /**
     * 设置是否检查您设置的容器大小
     * @param check
     */
    public void setCheckDrawPadSize(boolean check) {
        if (renderer != null && renderer.isRunning() == false) {
            renderer.setCheckDrawPadSize(check);
        } else {
            isCheckPadSize = check;
        }
    }

    /**
     * 获取容器宽度
     * @return
     */
    public int getPadWidth(){
        if(renderer!=null && renderer.isRunning()){
            return renderer.getPadWidth();
        }else{
            return 0;
        }
    }

    /**
     * 获取容器高度
     * @return
     */
    public int getPadHeight(){
        if(renderer!=null && renderer.isRunning()){
            return renderer.getPadHeight();
        }else{
            return 0;
        }
    }
}
