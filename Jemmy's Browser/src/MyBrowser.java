import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import java.io.*;
import java.util.ArrayList;


public class MyBrowser {//基于标签式的浏览器

    class url_address{
        String title;
        String url;

        public String getTitle(){return title;}
        public String getUrl(){return url;}
        public url_address(String title,String url){
            this.title = title;
            this.url = url;
        }
    }

    private volatile String newUrl = null;// 最新输入的链接
    private volatile boolean loadCompleted = false;//表示当前页面完全导入
    private volatile boolean openNewItem = false;//表示新的页面在新窗口中打开


    private TabItem tabItem_now;//当前标签项
    private Browser browser_now;//当前功能浏览器

    private String homePage = "cn.bing.com/";// 浏览器的首页

    private Button button_back;//后退按钮
    private Button button_forward;//向前按钮
    private Button button_go;//前进按钮
    private Button button_like;//收藏按钮
    private Button button_flush;//刷新按钮

    private Combo combo_address;// 地址栏
    ArrayList<url_address> url_list = new ArrayList<url_address>();
    private Browser browser_default = null;// 浏览窗口
    private ProgressBar progressBar_status;// 网页打开进度表，即页面导入情况栏
    private Label label_status;// 最终网页打开过程显示
    private TabFolder tabFolder;// Browser的容器
    private Composite composite_tool;// 工具栏区域
    private Composite composite_browser;// 浏览窗口区
    private Composite composite_status;// 状态栏区域
    protected Display display;
    protected Shell shell_default;

    private Menu menu;//菜单栏
    private MenuItem bookmark;//书签栏区域
    private Menu bookMarkMenu;//书签栏子菜单
    private MenuItem bookMarkItem;//书签栏下拉框中的每一个网址



    public static void main(String[] args) {
        try {
            MyBrowser window = new MyBrowser();
            window.open();//启动窗口
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void open() {//启动窗口的实现
        display = Display.getDefault();
        shell_default = new Shell(display);//shell窗口实现
        createContents();

        shell_default.open();
        shell_default.layout();
        while (!shell_default.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }


    protected void createContents() {
        shell_default.setSize(649, 448);
        shell_default.setText("Jemmy's Browser");
        GridLayout gl_shell = new GridLayout();
        gl_shell.marginWidth = 0;// 组件与容器边缘的水平距离
        gl_shell.marginHeight = 0;// 组件与容器边缘的垂直距离
        gl_shell.horizontalSpacing = 0;// 组件之间的水平距离
        gl_shell.verticalSpacing = 0;// 组件之间的垂直距离
        shell_default.setLayout(gl_shell);


        createMenu();
        createTool();
        createBrowser();
        createStatus();


        runThread();
    }

    private  void createMenu(){
        //设置菜单栏
        //通过shell对象和SWT.BAR样式值来创建一个菜单条
        menu = new Menu(shell_default,SWT.BAR);


        // 在菜单条的基础之上创建一个File的菜单
        MenuItem file = new MenuItem(menu,SWT.CASCADE);
        file.setText("文件");
        // 先在Shell上创建一个下拉框，然后将下拉框添加到文件菜单上
        Menu fileMenu = new Menu(shell_default,SWT.DROP_DOWN);
        file.setMenu(fileMenu);
        // 在下拉框上创建菜单项Open
        final MenuItem openItem = new MenuItem(fileMenu, SWT.CASCADE);
        openItem.setText("打开");

        openItem.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                FileDialog fileDialog = new FileDialog(shell_default);
                fileDialog.open();

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {

            }
        });


        // 在菜单条的基础之上创建一个Setting的菜单
        MenuItem edit = new MenuItem(menu,SWT.CASCADE);
        edit.setText("设置");
        // 先在Shell上创建一个下拉框，然后将下拉框添加到文件菜单上
        Menu editMenu = new Menu(shell_default,SWT.DROP_DOWN);
        edit.setMenu(editMenu);
        // 在下拉框上创建菜单项Settings
        final MenuItem setItem = new MenuItem(editMenu, SWT.CASCADE);
        setItem.setText("剪切");
        new MenuItem(editMenu,SWT.SEPARATOR);
        MenuItem copyItem = new MenuItem(editMenu,SWT.PUSH);
        copyItem.setText("复制");
        MenuItem pasteItem = new MenuItem(editMenu,SWT.PUSH);
        pasteItem.setText("粘贴");




        // 在菜单条的基础之上创建一个window的菜单
        MenuItem window = new MenuItem(menu,SWT.CASCADE);
        window.setText("窗口调整");
        // 先在Shell上创建一个下拉框，然后将下拉框添加到文件菜单上
        Menu windowsMenu = new Menu(shell_default,SWT.DROP_DOWN);
        window.setMenu(windowsMenu);
        // 在下拉框上创建菜单项Max、Min
        final MenuItem maxItem = new MenuItem(windowsMenu, SWT.CASCADE);
        maxItem.setText("Max");
        new MenuItem(windowsMenu,SWT.SEPARATOR);
        final MenuItem minItem = new MenuItem(windowsMenu,SWT.CASCADE);
        minItem.setText("Min");


        bookmark = new MenuItem(menu,SWT.CASCADE);
        bookmark.setText("书签栏");
        bookMarkMenu = new Menu(shell_default,SWT.DROP_DOWN);
        bookmark.setMenu(bookMarkMenu);


        MenuItem mail = new MenuItem(menu,SWT.CASCADE);
        mail.setText("邮件");//发送邮件功能实现
        Menu sendMenu = new Menu(shell_default, SWT.DROP_DOWN);
        mail.setMenu(sendMenu);
        MenuItem sendItem = new MenuItem(sendMenu, SWT.CASCADE);
        sendItem.setText("发送");
        sendItem.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                SendMail s = new SendMail();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {

            }

        });

        MenuItem download = new MenuItem(menu,SWT.CASCADE);
        download.setText("下载");//下载功能实现
        Menu downloadMenu = new Menu(shell_default,SWT.DROP_DOWN);
        download.setMenu(downloadMenu);
        final  MenuItem startItem = new MenuItem(downloadMenu,SWT.CASCADE);
        startItem.setText("开始/暂停");

        startItem.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                Download d = new Download();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {

            }
        });

        MenuItem help = new MenuItem(menu,SWT.CASCADE);
        help.setText("帮助");
        Menu helpMenu = new Menu(shell_default,SWT.DROP_DOWN);
        help.setMenu(helpMenu);
        MenuItem aboutItem = new MenuItem(helpMenu,SWT.PUSH);
        aboutItem.setText("关于");

        shell_default.setMenuBar(menu);
    }

    private void createTool() {
        //设置具体布局
        composite_tool = new Composite(shell_default, SWT.BORDER);
        // GridData()第一个参数是水平排列方式，第二个参数是垂直排列方式,第三个是水平抢占是否,第四个参数是垂直抢占是否
        GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_composite.heightHint = 30;// 高度和宽度
        gd_composite.widthHint = 549;
        composite_tool.setLayoutData(gd_composite);
        GridLayout fl_composite = new GridLayout();
        fl_composite.numColumns = 8;
        composite_tool.setLayout(fl_composite);

        button_back = new Button(composite_tool, SWT.NONE);
        button_back.setLayoutData(new GridData(27, SWT.DEFAULT));// 设置大小和格式
        button_back.setText("<-");

        button_forward = new Button(composite_tool, SWT.NONE);
        button_forward.setLayoutData(new GridData(24, SWT.DEFAULT));
        button_forward.setText("->");

        combo_address = new Combo(composite_tool, SWT.BORDER);
        final GridData gd_combo_3 = new GridData(SWT.FILL, SWT.LEFT, true,
                false);// 在窗口变化时，自动扩展水平方向的大小
        gd_combo_3.widthHint = 250;// 起始宽度
        gd_combo_3.minimumWidth = 30;// 设置最小宽度
        combo_address.setLayoutData(gd_combo_3);

        button_go = new Button(composite_tool, SWT.NONE);
        button_go.setLayoutData(new GridData(25, SWT.DEFAULT));
        button_go.setText("go");

        button_like = new Button(composite_tool,SWT.NONE);
        button_like.setLayoutData(new GridData(25,SWT.DEFAULT));
        button_like.setText("like");

        button_flush = new Button(composite_tool, SWT.NONE);
        button_flush.setLayoutData(new GridData(35, SWT.DEFAULT));
        button_flush.setText("flush");



        final Label label = new Label(composite_tool, SWT.SEPARATOR
                | SWT.VERTICAL);
        label.setLayoutData(new GridData(2, 17));

    }


    private void createBrowser() {//对于具体的浏览器布局进行设置
        composite_browser = new Composite(shell_default, SWT.NONE);
        final GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true,
                true);// 充满窗口,且水平和垂直方向随窗口而变
        gd_composite.heightHint = 273;
        composite_browser.setLayoutData(gd_composite);
        GridLayout gl_composite = new GridLayout();
        gl_composite.marginHeight = 0;// 使组件上下方向容器
        gl_composite.marginWidth = 0;// 使组件左右方向占满容器
        composite_browser.setLayout(gl_composite);

        tabFolder = new TabFolder(composite_browser, SWT.NONE);
        final GridData gd_tabFolder = new GridData(SWT.FILL, SWT.FILL, true,
                true);
        gd_tabFolder.heightHint = 312;
        gd_tabFolder.widthHint = 585;
        tabFolder.setLayoutData(gd_tabFolder);

        tabFolder.addMouseListener(new MouseAdapter(){
            //对于鼠标进行监听，通过Browser的容器
            @Override
            public void mouseUp(MouseEvent e) {
                if(e.button==3){//右键
                    Menu menu_itemRightMouse=new Menu(shell_default,SWT.POP_UP);
                    tabFolder.setMenu(menu_itemRightMouse);
                    MenuItem menuItem_itemClose=new MenuItem(menu_itemRightMouse,SWT.NONE);
                    menuItem_itemClose.setText("关闭当前标签");
                    menuItem_itemClose.addSelectionListener(new SelectionAdapter(){
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            if(tabFolder.getItemCount()!=1){//不是只存在一个标签的情况下
                                browser_now.dispose();
                                tabItem_now.dispose();
                                tabFolder.redraw();
                            }else{//只有一个标签
                                browser_now.setUrl(":blank");
                                browser_now.setText("");
                            }
                        }
                    });
                    MenuItem menuItem_itemCloseAll=new MenuItem(menu_itemRightMouse,SWT.NONE);
                    menuItem_itemCloseAll.setText("关闭所有标签");
                    menuItem_itemCloseAll.addSelectionListener(new SelectionAdapter(){
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            shell_default.close();
                        }
                    });
                }
            }
        });


        final TabItem tabItem_default = new TabItem(tabFolder, SWT.NONE);
        browser_default = new Browser(tabFolder, SWT.NONE);
        tabItem_default.setControl(browser_default);
        browser_default.setUrl(homePage);// 显示浏览器首页


        tabFolder.setSelection(tabItem_default);

    }



    private void createStatus() {
        composite_status = new Composite(shell_default, SWT.NONE);
        final GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true,
                false);// 参数true使状态栏可以自动水平伸缩
        gd_composite.heightHint = 18;
        gd_composite.widthHint = 367;
        composite_status.setLayoutData(gd_composite);
        GridLayout gl_composite = new GridLayout();
        gl_composite.numColumns = 2;
        gl_composite.marginBottom = 5;
        composite_status.setLayout(gl_composite);

        label_status = new Label(composite_status, SWT.NONE);
        GridData gd_status = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_status.heightHint = 18;
        gd_status.widthHint = 525;
        label_status.setLayoutData(gd_status);

        progressBar_status = new ProgressBar(composite_status, SWT.BORDER
                | SWT.SMOOTH);
        progressBar_status.setLayoutData(new GridData(75, 13));
        progressBar_status.setVisible(true);// 打开过程初始不可见

    }


    private void writeURLToFile(String fdir,String name,String title,String url) throws IOException {
        readURLFromFile("like", "like.txt");
        url_list.add(new url_address(title,url));

        File file_name = new File(fdir + "/" + name);

        BufferedWriter bw = null;

        if(file_name.exists()){
            bw = new BufferedWriter(new FileWriter(file_name));
        }

        for(int i = 0; i < url_list.size(); i++){
            bw.write(url_list.get(i).getTitle() +  "\t" + url_list.get(i).getUrl());
            bw.newLine();
        }

        if(bw!=null){
            bw.flush();
            bw.close();
        }

    }

    private  void readURLFromFile(String fdir,String name) throws IOException {
        url_list.clear();//将收藏的URL网址存进本地的文件中

        File file_dir = new File(fdir);
        if(!file_dir.exists()){
            file_dir.mkdir();
            System.out.println("Create Folder -->" + file_dir.toString());
        }

        File file_name = new File(fdir + "/"+ name);
        if(!file_name.exists()){
            file_name.createNewFile();
            System.out.println("Successful of creating file ---> Name=" + file_name.toString());
        }

        BufferedReader br = null;

        if(file_name.exists()){
            br = new BufferedReader( new FileReader(file_name));
        }else{
            System.out.println("File " + file_name.toString() +" does not exist!");

        }

        String s_line = new String();
        url_list.clear();



        if(br!=null){
            try{
                br.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void runThread() {


        button_back.setEnabled(false);
        button_forward.setEnabled(false);


        tabItem_now=tabFolder.getItem(tabFolder.getSelectionIndex());
        browser_now=(Browser) tabItem_now.getControl();


        tabFolder.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e) {
                TabItem temp=(TabItem) e.item;
                if(temp!=tabItem_now){//防止重选一个标签，预防多次触发相同事件
                    tabItem_now=temp;
                    browser_now=(Browser)tabItem_now.getControl();
                    //System.out.println("当前标签被修改了");//调试语句


                    if(browser_now.isBackEnabled()){//后退按钮的可用性
                        button_back.setEnabled(true);
                    }else{
                        button_back.setEnabled(false);
                    }
                    if(browser_now.isForwardEnabled()){//前进按钮的可用性
                        button_forward.setEnabled(true);
                    }else{
                        button_forward.setEnabled(false);
                    }

                }
            }
        });


        button_back.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (browser_now.isBackEnabled()){//本次可后退
                    browser_now.back();
                    button_forward.setEnabled(true);//下次可前进，前进按钮可用
                    //System.out.println("可后退");//调试语句
                }
                if(!browser_now.isBackEnabled()){//下次不可后退，后退按钮不可用
                    button_back.setEnabled(false);
                }
            }
        });

        button_forward.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (browser_now.isForwardEnabled()){//本次可前进
                    browser_now.forward();
                    button_back.setEnabled(true);//后退按钮可用
                    //System.out.println("可向前");//调试语句
                }
                if(!browser_now.isForwardEnabled()){//下次不可前进，前进按钮不可用
                    button_forward.setEnabled(false);
                }
            }
        });

        button_like.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                MessageBox messageBox = new MessageBox(shell_default,SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
                url_list.clear();
                if(combo_address.getText().indexOf("https") >= 0 ||
                        combo_address.getText().indexOf("http") >= 0){
                    messageBox.setMessage("添加到收藏夹 ---> "+ combo_address.getText());
                    messageBox.setText("添加到收藏夹");


                    if(messageBox.open() == SWT.OK){
                        String url_content = browser_now.getText();
                        //System.out.println(url_content);
                        int begin_titie_index = url_content.indexOf("<title>");
                        int end_title_index = url_content.indexOf("</title>");
                        String title_string = null;


                        if(begin_titie_index > 0 && end_title_index > 0){//搜索网址对应的标题
                            title_string = url_content.substring(begin_titie_index + 7,end_title_index);
                        }

                        bookMarkItem = new MenuItem(bookMarkMenu,SWT.PUSH);
                        bookMarkItem.setText(title_string);//将网址添加到书签栏中


                        //System.out.println(combo_address.getText());//调试语句
                        //System.out.println(title_string);//调试语句
                        //System.out.println(url_string);//调试语句
                        try{//将收藏的网址信息保存到本地中
                            writeURLToFile("like","like.txt",title_string,combo_address.getText());
                        }
                        catch(IOException e){
                            e.printStackTrace();
                        }

                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {

            }
        });

        button_flush.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                browser_now.refresh();
            }
        });

        combo_address.addKeyListener(new KeyAdapter() {// 手动输入地址栏后，按回车键转到相应网址
            @Override
            public void keyReleased(KeyEvent e) {
                String s = combo_address.getText();
                String baidu = "https://www.baidu.com/s?wd=";
                if(s.indexOf(".") > 0 ||
                        s.equals("localhost") || s.equals("127.0.0.1"))
                {//选择输入的网址进行访问
                    if (e.keyCode == SWT.CR) {//回车键触发事件
                        browser_now.setUrl(s);
                    }
                }
                else{//选择输入的内容使用百度进行搜索
                    String temp = baidu + s;
                    if(e.keyCode == SWT.CR)
                        browser_now.setUrl(temp);
                }

            }
        });


        browser_now.addLocationListener(new LocationAdapter() {
            @Override
            public void changing(LocationEvent e) {// 表示超级链接地址改变了
                if(openNewItem==false){//新的页面在同一标签中打开
                    button_back.setEnabled(true);//后退按钮可用,此句是后退按钮可用判定的逻辑开始点
                }
                //System.out.println("location_changing");// 调试语句
            }

            @Override
            public void changed(LocationEvent e) {// 找到了页面链接地址
                combo_address.setText(e.location);// 改变链接地址显示

                if(openNewItem==true){
                    openNewItem=false;
                }
                //System.out.println("location_changed");// 调试语句

            }

        });



        browser_now.addProgressListener(new ProgressAdapter() {
            @Override
            public void changed(ProgressEvent e) {//本事件不断发生于页面的导入过程中
                progressBar_status.setMaximum(e.total);// e.total表示从最开始页面到最终页面的数值
                progressBar_status.setSelection(e.current);
                if (e.current != e.total) {//页面还没完全导入
                    loadCompleted = false;
                    progressBar_status.setVisible(true);// 页面的导入情况栏可见
                } else {
                    loadCompleted = true;
                    progressBar_status.setVisible(true);// 页面导入情况栏不可见
                }
                //System.out.println("progress_changed");//调试语句

            }

            @Override
            public void completed(ProgressEvent arg0) {//发生在一次导入页面时,本监听器changed事件最后一次发生之前
                //System.out.println("progress_completed");//调试语句
            }
        });


        browser_now.addStatusTextListener(new StatusTextListener() {
            public void changed(StatusTextEvent e) {
                if (loadCompleted == false) {
                    label_status.setText(e.text);
                } else {
                    newUrl = e.text;//页面导入完成，捕捉页面上可能打开的链接
                }
                //System.out.println("statusText_changed");//调试语句
            }
        });


        browser_now.addTitleListener(new TitleListener() {
            public void changed(TitleEvent e) {
                shell_default.setText(e.title);
                if (e.title.length() > 3) {//显示当前页面提示字符在标签上
                    tabItem_now.setText(e.title.substring(0, 3) + "..");
                } else {
                    tabItem_now.setText(e.title);
                }
                tabItem_now.setToolTipText(e.title);//标签显示提示符
            }
        });


        browser_now.addOpenWindowListener(new OpenWindowListener() {// 在当前页面中打开点击的链接页面
            public void open(WindowEvent e) {
                Browser browser_new = new Browser(tabFolder, SWT.NONE);
                TabItem tabItem_new = new TabItem(tabFolder, SWT.NONE);
                tabItem_new.setControl(browser_new);
                tabFolder.setSelection(tabItem_new);//新打开的页面标签置顶
                tabFolder.redraw();//刷新容器
                browser_new.setUrl(newUrl);//新标签中设置新的链接地址
                openNewItem=true;//新的页面在新的标签中打开


                e.browser = browser_new;
                //System.out.println("OpenWindowListener_open");//调试语句


                display.syncExec(new Runnable(){
                    public void run() {
                        runThread();
                    }
                });


            }
        });


        browser_now.addCloseWindowListener(new CloseWindowListener(){
            public void close(WindowEvent e) {
                browser_now.dispose();
            }
        });

    }
}