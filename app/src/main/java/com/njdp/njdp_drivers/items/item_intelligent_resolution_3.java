package com.njdp.njdp_drivers.items;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.navisdk.adapter.BNOuterLogUtil;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.njdp.njdp_drivers.R;
import com.njdp.njdp_drivers.db.AppController;
import com.njdp.njdp_drivers.db.FieldInfoDao;
import com.njdp.njdp_drivers.db.SavedFieldInfoDao;
import com.njdp.njdp_drivers.slidingMenu;
import com.njdp.njdp_drivers.util.CommonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import bean.FieldInfo;
import bean.SavedFiledInfo;
import me.nereo.multi_image_selector.bean.Image;
import overlayutil.DrivingRouteOverlay;
import overlayutil.OverlayManager;

public class item_intelligent_resolution_3 extends Fragment implements View.OnClickListener{

    private static final String TAG = item_intelligent_resolution_3.class.getSimpleName();
    private slidingMenu mainMenu;
    private DrawerLayout menu;
    private SavedFieldInfoDao savedFieldInfoDao;
    private List<SavedFiledInfo> navigationDeploy=new ArrayList<SavedFiledInfo>();//选择的调配方案对应的农田信息
    private int count;
    private int deploy_tag;
    private CommonUtil commonUtil;


    //////////////////////////////////////////////////////////////////////////
    //////////////////////////////////地图用变量///////////////////////////////
    private TextureMapView mMapView;
    BaiduMap mBaiduMap;
    View markerpopwindow;

    //定位用变量
    /**
     * 当前定位的模式
     */
    private LocationService locationService;
    /**
     * 当前定位的模式
     */
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
    private boolean isFristLocation = true;
    //当前位置
    private BDLocation curlocation;


    //路径规划用变量
    RoutePlanSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    int nodeIndex = -1;//节点索引,供浏览节点时使用
    RouteLine route = null;
    OverlayManager routeOverlay = null;
    List<LatLng> points;
    List<Integer> colors;



    //导航
     /*
    *导航用变量
    * */
    public static List<Activity> activityList = new LinkedList<Activity>();
    private static final String APP_FOLDER_NAME = "BNSDK";
    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    private String mSDCardPath = null;

    ///////////////////////////////地图用变量结束//////////////////////////////
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ////////////////////////地图代码////////////////////////////////////
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getActivity().getApplicationContext());
        ////////////////////////地图代码结束////////////////////////////////////



        View view = inflater.inflate(R.layout.activity_1_intelligent_resolution_3, container,
                false);
        view.findViewById(R.id.getback).setOnClickListener(this);
        view.findViewById(R.id.menu).setOnClickListener(this);
        view.findViewById(R.id.my_location).setOnClickListener(this);
        view.findViewById(R.id.my_deploy).setOnClickListener(this);
        view.findViewById(R.id.replanning).setOnClickListener(this);
        view.findViewById(R.id.navigation).setOnClickListener(this);

        mainMenu=(slidingMenu)getActivity();
        menu=mainMenu.drawer;
        savedFieldInfoDao=new SavedFieldInfoDao(getActivity());

        try {
            navigationDeploy.addAll(savedFieldInfoDao.allFieldInfo());
        }catch (Exception e){
            Log.e(TAG,e.toString());
        };
        for(int i=0;i<navigationDeploy.size();i++)
        {
            Log.e(TAG,String.valueOf(navigationDeploy.get(i).getLatitude()));
            Log.e(TAG,String.valueOf(navigationDeploy.get(i).getLongitude()));
            Log.e(TAG,String.valueOf(navigationDeploy.get(i).getUser_name()));
        }

        commonUtil=new CommonUtil(mainMenu);
        count=navigationDeploy.size();
        Log.e(TAG,String.valueOf(count));

        //////////////////////////////Test Log
        for (int i=0;i<count;i++)
        {
            Log.e(TAG,String.valueOf(navigationDeploy.get(i).getId()));
            Log.e(TAG,navigationDeploy.get(i).getCropLand_site());
        }
        //////////////////////////////Test Log





        //////////////////////////////////////////////
        ////////////////地图代码/////////////////////
        //获取地图控件引用
        mMapView = (TextureMapView)view.findViewById(R.id.diaopeimapView);
        mMapView.showScaleControl(true);

        mBaiduMap = mMapView.getMap();


        // 开启图层定位
        // -----------location config ------------

        locationService = ((AppController) getActivity().getApplication()).locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的

        //注册监听
        locationService.registerListener(new mListener());
        locationService.setLocationOption(locationService.getOption());


        mBaiduMap.setMyLocationEnabled(true);
        locationService.start();// 定位SDK

        // 改变地图状态
        //MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(10);
        //mBaiduMap.setMapStatus(msu);

        //注册回到当前位置按钮监听事件
        ImageButton locationBtn = (ImageButton)view.findViewById(R.id.my_location);
        locationBtn.setOnClickListener(new goBackListener());

        ImageButton farmsLocationBtn = (ImageButton)view.findViewById(R.id.my_deploy);
        farmsLocationBtn.setOnClickListener(new goBackToPlanListener());

         /*
        * 导航用
        * */
        activityList.add(getActivity());
        BNOuterLogUtil.setLogSwitcher(true);
        if (initDirs()) {
            initNavi();
        }
        /*
        * 导航用结束
        * */
        /////////////////////地图代码结束/////////////////////////
        /////////////////////////////////////////////////////////

        return  view;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getback:
                clearDeploy();//清空方案数据
                mainMenu.getSupportFragmentManager().popBackStack();
                break;
            case R.id.menu:
                menu.openDrawer(Gravity.LEFT);
                break;
            case R.id.my_location:
                //我的位置
                break;
            case R.id.my_deploy:
                //我的方案
                break;
            case R.id.replanning:
                clearDeploy();//清空方案数据
                mainMenu.selectedFieldInfo.clear();
                mainMenu.addOrShowFragment(new item_intelligent_resolution());
                break;
            case R.id.navigation:
                mainMenu.addBackFragment(new item_intelligent_resolution_4());
                break;
            default:
                break;
        }
    }

    //清空储存的方案数据
    private void clearDeploy()
    {
        for (int i=0;i<count;i++)
        {
            SavedFiledInfo savedFiledInfo=navigationDeploy.get(i);
            savedFieldInfoDao.delete(savedFiledInfo);
            Log.e(TAG,"删除"+String.valueOf(i+1)+"次");
        }
    }


    ///////////////////////////地图代码/////////////////////////////
    ////////////////////////////////////////////////////////////////
    //回到当前位置按钮点击事件,将当前位置定位到屏幕中心
    class goBackListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            LatLng ll = new LatLng(curlocation.getLatitude(),
                    curlocation.getLongitude());
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);

        }
    }
    //回到方案路径按钮点击事件,将第一个位置定位到屏幕中心
    class goBackToPlanListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            drawPolyLine();
        }
    }

    //点击屏幕，关闭弹出窗口,现在实现不了
    class closeInfoWindowListener implements View.OnTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(markerpopwindow!=null){
                Log.i("ggggggg","ffffffffffff");
                mMapView.removeView(markerpopwindow);
            }
            return false;
        }
    };
    class mListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //保存当前location
            curlocation = location;

            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
            BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_geo);
            MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, false, mCurrentMarker);
            mBaiduMap.setMyLocationConfigeration(config);


            //调用画折线函数画线
            drawPolyLine();


            // 第一次定位时，将地图位置移动当前位置
            /*if (isFristLocation) {
                isFristLocation = false;

                //保存当前location
                curlocation = location;

                //LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
                //MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                //mBaiduMap.animateMapStatus(u);


                //调用画折线函数画线
                drawPolyLine();
            }*/
        }
    }

    private void drawRoutPlan(LatLng st,LatLng en,List<PlanNode> passby){
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(getRoutePlanResultListener);
        PlanNode stNode = PlanNode.withLocation(st);
        PlanNode enNode = PlanNode.withLocation(en);
        /*ECAR_AVOID_JAM
        驾车策略： 躲避拥堵
                ECAR_DIS_FIRST
        驾乘检索策略常量：最短距离
                ECAR_FEE_FIRST
        驾乘检索策略常量：较少费用
                ECAR_TIME_FIRST
        驾乘检索策略常量：时间优先*/
        DrivingRoutePlanOption drivingOption = new DrivingRoutePlanOption();
        //drivingOption.policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_FEE_FIRST);// 设置驾车路线策略
        mSearch.drivingSearch(drivingOption
                .policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_FEE_FIRST)
                .from(stNode)
                .passBy(passby)
                .to(enNode));
    }
    //画直线，如果驾车路径规划失败，则改画直线
    private void drawLine(List<Integer> colors,List<LatLng> points){
        OverlayOptions ooPolyline = new PolylineOptions().
                width(10)
                .colorsValues(colors)
                .points(points);
        //添加在地图中
        Polyline mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
    }
    private void drawPolyLine() {
        Integer[] marks = new Integer[]{R.drawable.icon_st, R.drawable.s2, R.drawable.s3, R.drawable.s4, R.drawable.s5,
                R.drawable.s6, R.drawable.s7, R.drawable.s8, R.drawable.s9, R.drawable.s10,R.drawable.s11,R.drawable.s12, R.drawable.s13,
                R.drawable.s14, R.drawable.s15, R.drawable.s16, R.drawable.s17, R.drawable.s18, R.drawable.s19, R.drawable.s20, R.drawable.s21,
                R.drawable.s22, R.drawable.s23, R.drawable.s24, R.drawable.s25, R.drawable.s26, R.drawable.s27, R.drawable.s28, R.drawable.s29,
                R.drawable.s30 };


        // 构造折线点坐标
        points = new ArrayList<LatLng>();
        points.add(new LatLng(36.63775364593931,114.46690856540684));
        points.add(new LatLng(36.636513928417486, 114.45600806797032));
        points.add(new LatLng(36.614884536027624,114.43781196201328));
        points.add(new LatLng(36.606410221575906, 114.42588149631504));
        points.add(new LatLng(36.600346725992225, 114.44261848056797));
        points.add(new LatLng(36.58814938811871, 114.46416198337559));
        points.add(new LatLng(36.572233768938496, 114.4745135307312));

        List<String> weizhis = new ArrayList<String>();
        weizhis.add("前郝村");
        weizhis.add("后郝村");
        weizhis.add("后百家村");
        weizhis.add("酒务楼村");
        weizhis.add("西大屯村");
        weizhis.add("庞村");
        weizhis.add("马庄村");


        //构建分段颜色索引数组
        colors = new ArrayList<>();
        colors.add(Integer.valueOf(Color.BLUE));
        colors.add(Integer.valueOf(Color.RED));
        colors.add(Integer.valueOf(Color.YELLOW));
        colors.add(Integer.valueOf(Color.GREEN));
        colors.add(Integer.valueOf(Color.BLACK));
        colors.add(Integer.valueOf(Color.BLUE));
        colors.add(Integer.valueOf(Color.RED));
        colors.add(Integer.valueOf(Color.YELLOW));
        colors.add(Integer.valueOf(Color.GREEN));
        colors.add(Integer.valueOf(Color.BLACK));
        colors.add(Integer.valueOf(Color.BLUE));
        colors.add(Integer.valueOf(Color.RED));
        colors.add(Integer.valueOf(Color.YELLOW));
        colors.add(Integer.valueOf(Color.GREEN));
        colors.add(Integer.valueOf(Color.BLACK));
        colors.add(Integer.valueOf(Color.BLUE));
        colors.add(Integer.valueOf(Color.RED));
        colors.add(Integer.valueOf(Color.YELLOW));
        colors.add(Integer.valueOf(Color.GREEN));
        colors.add(Integer.valueOf(Color.BLACK));



        ///////////////////////路径规划////////////////////////
        //获取第一个位置和最后一个位置,获取中间节点，进行驾车路径规划
        List<PlanNode> passby = new ArrayList<PlanNode>();
        if(navigationDeploy.size()>2) {
            for (int i = 1; i < navigationDeploy.size() - 1; i++) {
                LatLng ll = new LatLng(navigationDeploy.get(i).getLatitude(),navigationDeploy.get(i).getLongitude());
                PlanNode node = PlanNode.withLocation(ll);
                passby.add(node);
            }
        }
        LatLng llst = new LatLng(navigationDeploy.get(0).getLatitude(),navigationDeploy.get(0).getLongitude());
        LatLng llen = new LatLng(navigationDeploy.get(navigationDeploy.size()-1).getLatitude(),navigationDeploy.get(navigationDeploy.size()-1).getLongitude());
        this.drawRoutPlan(llst,llen,passby);



        //添加位置文字
        for (int i = 0; i < navigationDeploy.size(); i++) {
            //构建Marker图标
            int icon;
            if(i<30&&i!=navigationDeploy.size()-1){
                icon = marks[i];
            }else if(i==navigationDeploy.size()-1&&navigationDeploy.size()>1){
                icon=R.drawable.icon_en;
            } else{
                icon=R.drawable.icon_gcoding;
            }
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(icon);
            LatLng point = new LatLng(navigationDeploy.get(i).getLatitude(),navigationDeploy.get(i).getLongitude());
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(point)
                    .icon(bitmap)
                    .zIndex(15);
            //在地图上添加Marker，并显示
            Marker marker = (Marker) mBaiduMap.addOverlay(option);

            //构建文字Option对象，用于在地图上添加文字
            OverlayOptions textOption = new TextOptions()
                    .bgColor(0xAAFFFF00)
                    .fontSize(24)
                    .fontColor(0xFFFF00FF)
                    .text(navigationDeploy.get(i).getCropLand_site()+"")
                    .rotate(-30)
                    .zIndex(14)
                    .position(point);
            //在地图上添加该文字对象并显示
            mBaiduMap.addOverlay(textOption);

            FieldInfo info = new FieldInfo();
            info.setArea_num(navigationDeploy.get(i).getArea_num());
            info.setVillage(navigationDeploy.get(i).getCropLand_site());
            info.setUnit_price(navigationDeploy.get(i).getUnit_price());
            info.setStart_time(navigationDeploy.get(i).getStart_time());
            info.setEnd_time(navigationDeploy.get(i).getEnd_time());
            info.setUser_name(navigationDeploy.get(i).getUser_name());
            info.setLatitude(point.latitude);
            info.setLongitude(point.longitude);
            info.setTelephone(navigationDeploy.get(i).getUser_name());
            info.setQq(navigationDeploy.get(i).getQq());

            Bundle bundle = new Bundle();
            bundle.putSerializable("info", info);
            marker.setExtraInfo(bundle);

            //添加覆盖物鼠标点击事件
            mBaiduMap.setOnMarkerClickListener(new markerClicklistener());
        }
    }

    //路径规划监听
    OnGetRoutePlanResultListener getRoutePlanResultListener = new OnGetRoutePlanResultListener() {
        public void onGetWalkingRouteResult(WalkingRouteResult result) {
            //获取步行线路规划结果

        }
        public void onGetTransitRouteResult(TransitRouteResult result) {
            //获取公交换乘路径规划结果
        }
        public void onGetDrivingRouteResult(DrivingRouteResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
//                commonUtil.error_hint("抱歉，未找到结果");
                Log.d(TAG, "抱歉，未找到结果");

                //驾车路径规划失败，则画直线,现在不起用
                //drawLine(colors,points);

            }
            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                //result.getSuggestAddrInfo()
                return;
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                nodeIndex = -1;
                route = result.getRouteLines().get(0);
                route = result.getRouteLines().get(0);
                DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);
                routeOverlay = overlay;
                mBaiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            }
        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

        }
    };


    //导航
    //地图图标点击事件监听类
    class markerClicklistener implements BaiduMap.OnMarkerClickListener {

        /**
         * 地图 Marker 覆盖物点击事件监听函数
         *
         * @param marker 被点击的 marker
         */
        @Override
        public boolean onMarkerClick(Marker marker) {
            final FieldInfo info = (FieldInfo) marker.getExtraInfo().get("info");
            InfoWindow infoWindow;

            //构造弹出layout
            LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());
            markerpopwindow = inflater.inflate(R.layout.markerpopwindow2, null);

            TextView tv = (TextView) markerpopwindow.findViewById(R.id.markinfo);
            String markinfo = "位置:"+info.getVillage()+"\n"+
                    "电话:" + info.getUser_name() + "\n" +
                    "面积:" + info.getArea_num() + "\n" +
                    "单价:"+info.getUnit_price()+"\n"+
                    "开始时间:" + info.getStart_time()+"\n"+
                    "结束时间:"+info.getEnd_time();
            Log.i("markinfo", markinfo);
            tv.setText(markinfo);


            ImageButton tellBtn = (ImageButton) markerpopwindow.findViewById(R.id.markerphone);
            tellBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + info.getTelephone()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });

            /*Button qqBtn = (Button) markerpopwindow.findViewById(R.id.gotoqq);
            qqBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url="mqqwpa://im/chat?chat_type=wpa&uin="+info.getQq();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            });*/

            /*
            * 导航使用
            * */
            ImageButton gohereBtn = (ImageButton) markerpopwindow.findViewById(R.id.markergohere);
            gohereBtn.setOnClickListener(new GohereListener(info));
            /*导航使用结束结束*/

            LatLng ll = marker.getPosition();
            //将marker所在的经纬度的信息转化成屏幕上的坐标
            Point p = mBaiduMap.getProjection().toScreenLocation(ll);
            p.y -= 90;
            LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);
            //初始化infoWindow，最后那个参数表示显示的位置相对于覆盖物的竖直偏移量，这里也可以传入一个监听器
            infoWindow = new InfoWindow(markerpopwindow, llInfo, 0);
            mBaiduMap.showInfoWindow(infoWindow);//显示此infoWindow
            //让地图以备点击的覆盖物为中心
            MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.setMapStatus(status);
            return true;
        }
    }


    /*
    点击去这里按钮进行导航监听类
    * */
    class GohereListener implements View.OnClickListener {
        private FieldInfo info;

        public GohereListener(FieldInfo info) {
            this.info = info;
        }

        @Override
        public void onClick(View v) {
            //使用百度地图进行导航
            routeplanToNavi();
        }

        private void routeplanToNavi() {
            BNRoutePlanNode sNode = null;
            BNRoutePlanNode eNode = null;
            sNode = new BNRoutePlanNode(curlocation.getLongitude(), curlocation.getLatitude(), "", null, BNRoutePlanNode.CoordinateType.BD09LL);
            eNode = new BNRoutePlanNode(info.getLongitude(), info.getLatitude(), "", null, BNRoutePlanNode.CoordinateType.BD09LL);

            if (sNode != null && eNode != null) {
                List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
                list.add(sNode);
                list.add(eNode);
                /*发起算路操作并在算路成功后通过回调监听器进入导航过程.
                 *参数:
                 *activity - 建议是应用的主Activity
                 *nodes - 传入的算路节点，顺序是起点、途经点、终点，其中途经点最多三个，参考 BNRoutePlanNode
                 *preference - 算路偏好，参考RoutePlanPreference定义 [推荐:1,高速优先(用时最少):2,少走高速（路径最短）:4,少收费:8,躲避拥堵:16]
                 *isGPSNav - true表示真实GPS导航，false表示模拟导航
                 *listener - 开始导航回调监听器，在该监听器里一般是进入导航过程页面
                 * */
                BaiduNaviManager.getInstance().launchNavigator(getActivity(), list, 2, true, new GohereRoutePlanListener(sNode));

            }
        }
    }

    class GohereRoutePlanListener implements BaiduNaviManager.RoutePlanListener {
        private BNRoutePlanNode mBNRoutePlanNode = null;

        public GohereRoutePlanListener(BNRoutePlanNode node) {
            mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {
            /*
             * 设置途径点以及resetEndNode会回调该接口
			 */
            for (Activity ac : activityList) {

                if (ac.getClass().getName().endsWith("BNDGuideActivity")) {

                    return;
                }
            }

            Intent intent = new Intent(getActivity(), BNDGuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            startActivity(intent);

        }

        @Override
        public void onRoutePlanFailed() {
            Toast.makeText(getActivity(), "算路失败", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    * 导航使用
    * */
    //初始化导航用文件件
    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    //得到SD卡路径
    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    //初始化导航配置
    String authinfo = null;

    private void initNavi() {
        BNOuterTTSPlayerCallback ttsCallback = null;
        BaiduNaviManager.getInstance().init(getActivity(), mSDCardPath, APP_FOLDER_NAME, new BaiduNaviManager.NaviInitListener() {
            @Override
            public void onAuthResult(int i, String s) {
                if (0 == i) {
                    authinfo = "key校验成功!";
                } else {
                    authinfo = "key校验失败, " + s;
                }

                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), authinfo, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void initStart() {
                Toast.makeText(getActivity(), "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void initSuccess() {
                Toast.makeText(getActivity(), "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                initSetting();
            }

            @Override
            public void initFailed() {
                Toast.makeText(getActivity(), "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
            }
        }, null, ttsHandler, null);
    }

    private void initSetting() {
        BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
        BNaviSettingManager.setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
        BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
    }

    /**
     * 内部TTS播报状态回传handler
     */
    private Handler ttsHandler = new Handler() {
        public void handleMessage(Message msg) {
            int type = msg.what;
            switch (type) {
                case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
                    Toast.makeText(getActivity(), "Handler : TTS play start", Toast.LENGTH_SHORT).show();
                    //showToastMsg("Handler : TTS play start");
                    break;
                }
                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
                    Toast.makeText(getActivity(), "Handler : TTS play end", Toast.LENGTH_SHORT).show();
                    //showToastMsg("Handler : TTS play end");
                    break;
                }
                default:
                    break;
            }
        }
    };

    ////////////////////////////地图代码结束//////////////////////////
    //////////////////////////////////////////////////////////////

}
