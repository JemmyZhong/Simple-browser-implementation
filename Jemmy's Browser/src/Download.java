import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Download extends JFrame {
    public Download(){
        super("下载专区");
        setSize(350,220);
        InitDownloadPane();
        InitFavorPane();
        setVisible(true);
    }
    String savepath = "E:/Eclipse/Jemmy's Browser/";
    JPanel DownloadPane;
    JPanel FavorPane;
    JTextField urlField;

    private void InitFavorPane(){
        FavorPane = new JPanel();
        FavorPane.setLayout(new GridLayout(20,1));
        FavorPane.add(new JLabel("---------------我的收藏----------------"));
        JButton clear = new JButton("清空收藏");
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                while(FavorPane.getComponentCount()>2)
                    FavorPane.remove(FavorPane.getComponentCount()-1);
                FavorPane.setVisible(false);
                FavorPane.setVisible(true);
            }
        });
        FavorPane.add(clear);
        FavorPane.setVisible(false);

    }

    private void InitDownloadPane(){
        DownloadPane = new JPanel();
        DownloadPane.setLayout(new GridLayout(6,1));
        DownloadPane.add(new JLabel("---------------下载管理----------------"));
        JButton clear = new JButton("清空下载记录");
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                while(DownloadPane.getComponentCount()>2)
                    DownloadPane.remove(FavorPane.getComponentCount());
                DownloadPane.setVisible(false);
                DownloadPane.setVisible(true);
            }
        });
        DownloadPane.add(clear);
        DownloadPane.setVisible(true);

        getContentPane().add(DownloadPane);

    }

    class Favorcomfirm extends JFrame{

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public Favorcomfirm(){

            this.setLayout(new GridLayout(3,2));

            JLabel setname = new JLabel("书签命名：");
            JTextField nameTF = new JTextField();
            nameTF.setText(urlField.getText());
            nameTF.setSelectionStart(0);
            JLabel url1 = new JLabel("目标URL：");
            JLabel url2 = new JLabel(urlField.getText());
            JButton OK = new JButton("OK");
            OK.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    addFavor(nameTF.getText(),urlField.getText());
                    FavorPane.setVisible(false);
                    FavorPane.setVisible(true);
                }
            });
            JButton NO = new JButton("CANCAL");
            NO.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){

                }
            });

            this.add(setname);this.add(nameTF);this.add(url1);
            this.add(url2);this.add(OK);this.add(NO);
            this.pack();
        }
    }

    private void openFavor(){
        this.getContentPane().add(FavorPane, BorderLayout.EAST);

        DownloadPane.setVisible(false);

        if(!FavorPane.isVisible())
            FavorPane.setVisible(true);
        else
            FavorPane.setVisible(false);
    }

    private void addFavor(String name,String url){
        JButton New = new JButton(name);
        New.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                Judge(url);
            }
        });
        New.setBackground(Color.WHITE);
        FavorPane.add(New);

    }
    private void openDownload(){
        this.getContentPane().add(DownloadPane, BorderLayout.EAST);
        FavorPane.setVisible(false);

        if(!DownloadPane.isVisible())
            DownloadPane.setVisible(true);
        else
            DownloadPane.setVisible(false);
    }

    public class download extends Thread{
        String filename;
        HttpURLConnection HUC;
        float now;
        float length;
        File file;
        downloadwork work;
        int flag = 1;
        public download(String f,HttpURLConnection huc){
            this.filename = f;
            this.HUC = huc;
            file = new File(savepath+filename);
        }
        public void run(){
            try {
                //downloadwork为一个用于放置下载信息、
                //下载进度，下载管理的小Panel，当有新的任务到来，
                //就会往下载管理的那个Pane里放置一个对应的小Panel
                work = new downloadwork(filename,HUC);
                DownloadPane.add(work);
                InputStream IS = HUC.getInputStream();
                FileOutputStream FOS = new FileOutputStream(file);
                byte buffer[] = new byte[10];

                while(IS.read(buffer) != -1 ){
                    while(flag==0){
                        System.out.println(flag);
                    }
                    FOS.write(buffer);
                    now =  file.length();
                    now = now/1024;
                    now = now*100/length;
                    //实时更新下载的百分比进度
                    work.pro1.setText(String.valueOf(now)+"%");
                    work.repaint();
                }
                FOS.flush();
                FOS.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        public class downloadwork extends JPanel{
            /**
             *
             */
            private static final long serialVersionUID = 1L;
            String filename;
            HttpURLConnection HUC;
            JLabel status1 ;
            JLabel status2 ;
            JLabel pro1;

            public downloadwork(String filename,HttpURLConnection HUC){
                this.filename = filename;
                this.HUC = HUC;
                this.setLayout(new GridLayout(4,2));
                File f = new File(savepath+filename);

                JLabel name1 = new JLabel("文件名：");
                JLabel name2 = new JLabel(filename);
                length = HUC.getContentLength();
                length = length/1024;
                pro1 = new JLabel("0");
                JLabel pro2 = new JLabel("文件大小： "+length+" KB");
                status1 = new JLabel("下载中");
                status2 = new JLabel("…………");

                JButton ctr1 = new JButton("暂停");
                ctr1.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(flag == 1)
                            flag = 0;
                        if(flag == 2)
                            HUC.disconnect();
                    }
                });
                JButton ctr2 = new JButton("继续");
                ctr2.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(flag == 0){
                            flag = 1;
                        }
                        else if(!f.exists() && flag == 2){
                            //若不存在，则直接下载
                            download cut = new download(filename,HUC);
                            cut.start();
                        }
                        else if(flag ==2){
                            //若文件已存在，则获取其长度/大小
                            HUC.setRequestProperty("Range", "bytes="+now);

                            //若本地文件与资源文件同大小则OK
                            if(now == length){
                                status1.setText("下载完成");
                                status2.setText("       ");
                            }
                            else{
                                download cut = new download(filename,HUC);
                                cut.start();
                            }
                        }
                    }
                });
                this.add(name1);this.add(name2);this.add(pro1);this.add(pro2);
                this.add(status1);this.add(status2);this.add(ctr1);this.add(ctr2);
            }
        }
    }
    private void downloadconfirm(String name,HttpURLConnection huc){
        String message = "文件名："+name+" 大小："+(huc.getContentLength()/1024)+"kb" +"\n 确认下载?";
        int selection = JOptionPane.showConfirmDialog(this,message,"下载管理",
                JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
        if(selection == JOptionPane.OK_OPTION){
            openDownload();
            download mission = new download(name,huc);
            mission.start();
        }
    }

    private void Judge(String s){
        String status = "本次加载使用了缓存";
        try {
            if (!s.startsWith("http"))//默认HTTP
                s = "http://" + s;

            URL u = new URL(s);
            HttpURLConnection HUC = (HttpURLConnection)u.openConnection();
            HUC.setConnectTimeout(10000);//设置连接超时

            //获取服务器端的响应码、响应文件类型、响应报文
            String Type = HUC.getContentType();
            int rescode = HUC.getResponseCode();
            String resmes = HUC.getResponseMessage();

            //对资源文件命名，方便储存
            String filename = s;
            filename = filename.substring(filename.lastIndexOf("/")+1);
            //若为文本类型，则将其视为html，下载并显示
            if(Type.startsWith("text")){
                //对文件名字进行进一步编辑
                if(filename.contains("."))
                    filename = filename.substring(0,filename.lastIndexOf("."));
                filename = filename + "." + Type.substring(Type.indexOf("/")+1);
                if(filename.indexOf(";")!=- 1)
                    filename = filename.substring(0,filename.indexOf(";"));
                if(filename.length()>=15)
                    filename = filename.substring(15);
                String target = savepath + filename;

                File f = new File(target);
                //检查是否有缓存文件，若无，则从头全新加载
                if(!f.exists()){
                    status = "本次加载无缓存";
                    FileOutputStream FOS = new FileOutputStream(f);
                    if(rescode>=200 && rescode<300){

                        InputStream IS = HUC.getInputStream();
                        byte buffer[] = new byte[1024];
                        while(IS.read(buffer) != -1)
                            FOS.write(buffer);
                    }
                    else{
                        FOS.write(HUC.getHeaderField(0).getBytes());
                        FOS.flush();
                    }
                }

            }

            else  //若文件类型为text以外的类型（如JPG、ZIP），则跳转到下载方法。
                downloadconfirm(filename,HUC);



        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
