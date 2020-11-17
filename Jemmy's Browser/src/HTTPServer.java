import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HTTPServer{
    public HTTPServer(){
        //使用线程池进行调用多线程
        ExecutorService pool = Executors.newFixedThreadPool(100);
        try {
            ServerSocket server = new ServerSocket(80);
            System.out.println("服务器已启动完毕，正等待客户端进行连接。。。");
            while(true)
            {
                //客户端请求时，启动一个线程
                ThreadForServer tfs = new ThreadForServer(server.accept());
                pool.execute(tfs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new HTTPServer();
    }

    class ThreadForServer extends Thread{
        Socket client = null;//客户端
        BufferedReader in = null;//缓冲的阅读器
        BufferedWriter out = null;//缓冲的书写器
        //服务器存储文件的路径
        String rootDirectory = "E:/Eclipse/Jemmy's Browser/src/HTTPServer";

        //服务器线程的构造方法
        public ThreadForServer(Socket socket) throws IOException{
            this.client = socket;
            this.in = new BufferedReader(new
                    InputStreamReader(client.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter
                    (client.getOutputStream()));
            //查看客户端的使用的线程号
            System.out.println("让地址为：" + client.getInetAddress() +
                    " 使用线程： " + this.getName());
        }

        //重写run方法
        public void run() {
            try {
                //获取客户端的请求
                String request = in.readLine();
                System.out.println("接受到客户端的请求为： "+ request.toString());

                //获取客户端请求方式
                String [] requestStrings = request.split("\\s+");
                String method = requestStrings[0];

                //获取客户端请求的文件
                String fileName = requestStrings[1];
                if(fileName.endsWith("/"))
                    fileName = "index.html";
                File requestFile = new File(rootDirectory,fileName);

                //获取HTTP的版本号
                String version = "";
                if(requestStrings.length > 2)
                    version = requestStrings[2];

                //响应客户端的HTTP请求
                if(requestFile.exists())//若请求的文件在服务器中存在
                {//获取文件的大小以及文件类型
                    String contentType = URLConnection.getFileNameMap()
                            .getContentTypeFor(fileName);
                    byte[] data = Files.readAllBytes(requestFile.toPath());

                    //处理客户端的HTTP请求： GET/POST/HEADER 方式
                    if(method.equals("HEAD"))//发送报文头
                        sendHeader("HTTP/1.0 200 OK",
                                contentType,data.length,"");
                    else if(method.equals("GET"))
                    {
                        //先发送报文头
                        sendHeader("HTTP/1.0 200 OK",contentType,data.length,"");
                        OutputStream raw = new BufferedOutputStream
                                (client.getOutputStream());
                        raw.write(data);
                        raw.flush();
                    }
                    else if(method.equals("POST"))
                    {

                        System.out.println("请输入您想要查询的用户昵称以及密码！");
                        System.out.println("格式如下： name:+输入&pass: +输入");
                        String userAndPass = in.readLine();
                        System.out.println(userAndPass);
                        BufferedReader buff = new BufferedReader
                                (new FileReader
                                        (new File(rootDirectory,"database.txt")));
                        //读取数据库中每个用户的信息
                        String line = "";
                        String userName = "";
                        while((line = buff.readLine()) != null)
                        {
                            if(line.startsWith(userAndPass))
                            {
                                userName = line.substring
                                        (line.indexOf(':') + 1,line.length());
                                break;
                            }
                        }
                        //发送报文头
                        sendHeader("HTTP/1.0 200 OK",userName.getClass()
                                .toString(),userName.length(),userAndPass.substring
                                (0,userAndPass.indexOf('&')) + "-" + userName);
                        //发送请求的用户名
                        System.out.println(userName);
                        out.write(userName);
                        out.flush();
                    }
                }
                else {//客户端请求的文件不存在时，返回MIME首部以及404网页
                    String body = new StringBuffer("<html>\r\n")
                            .append("<head><title>File Not Found</title>\r\n")
                            .append("</head>\r\n")
                            .append("<body>")
                            .append("<h1>HTTP Error 404: File Not Found</h1>\r\n")
                            .append("</body></html>\r\n").toString();

                    if(version.startsWith("HTTP/"))
                        sendHeader("HTTP/1.0 404 File Not Found"
                                ,"text/html; charset=utf-8",body.length(),"empty");
                    out.write(body);
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    client.close();
                    System.out.println("线程 " + this.getName() + " 运行结束");
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        //发送报文头的函数实现
        public void sendHeader(String responseCode,String contentType,
                               int length,String cookie) throws IOException {
            StringBuffer sb = new StringBuffer();
            Date now = new Date();
            sb.append(responseCode + "\r\n")
                    .append("Date: " + now + "\r\n")
                    .append("Server: LocalHost 2.0\r\n")
                    .append("Content-length: " + length + "\r\n")
                    .append("Content-type: " + contentType + "\r\n")
                    .append("set-Cookie:" + cookie + "\r\n");
            //cookie = userNo-userName

            System.out.println("响应客户端请求： \n" + sb.toString());
            sb.append("\r\n");
            out.write(sb.toString());
            out.flush();
        }
    }
}