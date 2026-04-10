package rtc;

import static owt.base.MediaCodecs.VideoCodec.H264;
import static owt.base.MediaCodecs.VideoCodec.H265;
import static owt.base.MediaCodecs.VideoCodec.VP8;
import static owt.base.MediaCodecs.VideoCodec.VP9;

import android.content.Context;

import rtc.utils.WKRTCManager;

import org.webrtc.EglBase;
import org.webrtc.Logging;
import org.webrtc.PeerConnection;

import java.lang.reflect.Method;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import owt.base.ContextInitialization;
import owt.base.VideoEncodingParameters;
import owt.p2p.P2PClient;
import owt.p2p.P2PClientConfiguration;
import owt.p2p.RemoteStream;
import rtc.p2p.SocketSignalingChannel;
import rtc.utils.WKLogger;

/**
 * 4/30/21 2:21 PM
 * rtc
 */
public class WKRTCApplication {
    private WKRTCApplication() {
    }

    private static final class LiMRTCApplicationBinder {
        private static final WKRTCApplication rtc = new WKRTCApplication();
    }

    public static WKRTCApplication getInstance() {
        return LiMRTCApplicationBinder.rtc;
    }

    private WeakReference<Context> context;
    private EglBase rootEglBase;

    // OWT 服务器地址 (局域网)
    public String serverUrl = "https://owt.yujj888.top";
    public String turnIP = "103.214.172.45000";
    List<PeerConnection.IceServer> trunList = new ArrayList<>();

    public Context getContext() {
        return context.get();
    }

    public RemoteStream remoteStream;

    public EglBase getRootEglBase() {
        return rootEglBase;
    }

    public void setDebug(boolean isDebug) {
        WKLogger.isDebug = isDebug;
    }

    public void initModule(Context context, List<PeerConnection.IceServer> trunList) {
//        this.serverUrl = url;
//        this.turnIP = turnIP;
        this.trunList = trunList;
        if (trunList == null) {
            this.trunList = new ArrayList<>();
        }
        this.context = new WeakReference<>(context);
        
        // 尝试多种方式创建 EglBase
        rootEglBase = createEglBaseCompat();
        
        if (rootEglBase != null) {
            ContextInitialization.create()
                    .setApplicationContext(context)
                    .addIgnoreNetworkType(ContextInitialization.NetworkType.LOOPBACK)
                    .setVideoHardwareAccelerationOptions(
                            rootEglBase.getEglBaseContext(),
                            rootEglBase.getEglBaseContext())
                    .initialize();
            Logging.enableLogToDebugOutput(Logging.Severity.LS_ERROR);
        } else {
            // 无法创建 EglBase，仅初始化基本上下文
            WKLogger.e("WKRTCApplication", "EglBase creation failed, RTC may not work properly");
            ContextInitialization.create()
                    .setApplicationContext(context)
                    .addIgnoreNetworkType(ContextInitialization.NetworkType.LOOPBACK)
                    .initialize();
        }
    }
    
    /**
     * 兼容性方法创建 EglBase
     */
    private EglBase createEglBaseCompat() {
        // 方法1: 直接调用接口静态方法 (可能在某些设备上失败)
        try {
            return EglBase.create();
        } catch (Throwable t1) {
            WKLogger.e("WKRTCApplication", "Method 1 failed: " + t1.getMessage());
        }
        
        // 方法2: 通过反射调用 create()
        try {
            Method createMethod = EglBase.class.getMethod("create");
            return (EglBase) createMethod.invoke(null);
        } catch (Throwable t2) {
            WKLogger.e("WKRTCApplication", "Method 2 failed: " + t2.getMessage());
        }
        
        // 方法3: 通过反射调用 createEgl14(int[])
        try {
            Method createEgl14Method = EglBase.class.getMethod("createEgl14", int[].class);
            return (EglBase) createEgl14Method.invoke(null, (Object) EglBase.CONFIG_PLAIN);
        } catch (Throwable t3) {
            WKLogger.e("WKRTCApplication", "Method 3 failed: " + t3.getMessage());
        }
        
        // 方法4: 直接实例化 EglBase14Impl 通过反射
        try {
            Class<?> egl14ImplClass = Class.forName("org.webrtc.EglBase14Impl");
            java.lang.reflect.Constructor<?> constructor = egl14ImplClass.getDeclaredConstructor(
                    android.opengl.EGLContext.class, int[].class);
            constructor.setAccessible(true);
            return (EglBase) constructor.newInstance(android.opengl.EGL14.EGL_NO_CONTEXT, EglBase.CONFIG_PLAIN);
        } catch (Throwable t4) {
            WKLogger.e("WKRTCApplication", "Method 4 failed: " + t4.getMessage());
        }
        
        // 方法5: 直接实例化 EglBase10Impl 通过反射
        try {
            Class<?> egl10ImplClass = Class.forName("org.webrtc.EglBase10Impl");
            java.lang.reflect.Constructor<?> constructor = egl10ImplClass.getDeclaredConstructor(
                    javax.microedition.khronos.egl.EGLContext.class, int[].class);
            constructor.setAccessible(true);
            return (EglBase) constructor.newInstance(null, EglBase.CONFIG_PLAIN);
        } catch (Throwable t5) {
            WKLogger.e("WKRTCApplication", "Method 5 failed: " + t5.getMessage());
        }
        
        return null;
    }

//    P2PClient p2PClient;

    public P2PClient getP2PClient() {
//        if (p2PClient == null) initP2PClient();
//        return p2PClient;
        return initP2PClient();
    }

    private P2PClient initP2PClient() {

//        PeerConnection.IceServer iceServer = PeerConnection.IceServer.builder(
//                "turn:" + turnIP + ":3478?transport=udp").setUsername("tsdd").setPassword(
//                "tsddpwd").createIceServer();
////        PeerConnection.IceServer iceServer11 = PeerConnection.IceServer.builder(
////                "turn:162.209.218.50:3478?transport=udp").setUsername("user").setPassword(
////                "passwd").createIceServer();
//        PeerConnection.IceServer iceServer0 = PeerConnection.IceServer.builder(
//                "stun:stun.qq.com").createIceServer();
//        PeerConnection.IceServer iceServer1 = PeerConnection.IceServer.builder(
//                "stun:stun1.l.google.com:19302").createIceServer();
//        PeerConnection.IceServer iceServer2 = PeerConnection.IceServer.builder(
//                "stun:stun2.l.google.com:19302").createIceServer();
//        PeerConnection.IceServer iceServer3 = PeerConnection.IceServer.builder(
//                "stun:stunserver.org").createIceServer();
//        PeerConnection.IceServer iceServer4 = PeerConnection.IceServer.builder(
//                "stun:stun.xten.com").createIceServer();
//        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
//        iceServers.add(iceServer);
//        iceServers.add(iceServer0);
//        iceServers.add(iceServer1);
//        iceServers.add(iceServer2);
//        iceServers.add(iceServer3);
//        iceServers.add(iceServer4);
//        iceServers.add(iceServer11);
        PeerConnection.RTCConfiguration rtcConfiguration = new PeerConnection.RTCConfiguration(
                trunList);
        rtcConfiguration.bundlePolicy = PeerConnection.BundlePolicy.MAXCOMPAT;
        rtcConfiguration.useMediaTransport = true;
//        rtcConfiguration.maxIPv6Networks = 2;
        VideoEncodingParameters h264 = new VideoEncodingParameters(H264);
        VideoEncodingParameters h265 = new VideoEncodingParameters(H265);
        VideoEncodingParameters vp8 = new VideoEncodingParameters(VP8);
        VideoEncodingParameters vp9 = new VideoEncodingParameters(VP9);
        P2PClientConfiguration configuration = P2PClientConfiguration.builder()
                .addVideoParameters(h264)
                .addVideoParameters(vp8)
                .addVideoParameters(vp9)
                .addVideoParameters(h265)
                .setRTCConfiguration(rtcConfiguration)
                .build();
        return new P2PClient(configuration, new SocketSignalingChannel(() -> WKRTCManager.getInstance().getIRTCListener().onPublish()));
    }

}
