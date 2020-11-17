import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

public class SendMail extends JFrame {
    //邮件信息
    private JTextField mailSender;
    private JTextField mailRecive;
    private JTextField mailTitle;
    private JTextField mailPassword;
    private JTextField mailMessage;
    private JPanel mailPane;

    public SendMail(){
        super("邮件");
        setSize(400,320);
        InitMailPane();
        this.getContentPane().add(mailPane);
        this.setVisible(true);
    }


    public void InitMailPane()
    {
        mailPane = new JPanel();
        mailPane.setLayout(new BorderLayout());
        JPanel mesPane = new JPanel(new java.awt.GridLayout(4,1));
        mailSender = new JTextField();
        mailRecive = new JTextField();
        mailTitle = new JTextField();
        mailPassword = new JTextField();
        mesPane.add(mailSender);
        mesPane.add(mailPassword);
        mesPane.add(mailRecive);
        mesPane.add(mailTitle);
        mailSender.setText("发件人地址");
        mailPassword.setText("发件人邮箱密码");
        mailRecive.setText("收件人地址");
        mailTitle.setText("------标题------");
        mailMessage = new JTextField();
        mesPane.add(mailMessage);
        mailMessage.setText("正文内容");

        JButton send = new JButton("发送");
        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMail(mailSender.getText(),mailPassword.getText(),
				mailRecive.getText(),mailTitle.getText(),mailMessage.getText());
                mailSender.setText("发件人地址");
                mailPassword.setText("发件人邮箱密码");
                mailRecive.setText("收件人地址");
                mailTitle.setText("------标题------");
                mailMessage.setText("正文内容");
            }
        });
        mailPane.add(mesPane,BorderLayout.NORTH);
        mailPane.add(mailMessage,BorderLayout.CENTER);
        mesPane.add(send,BorderLayout.SOUTH);
        mailPane.setVisible(true);
    }
    private  void sendMail(String sender,String password,
	String receive,String title,String message) {
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.host", "smtp.qq.com");
        properties.put("mail.smtp.port", 465);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.debug", "true");

        try {
            Session session = Session.getInstance(properties);
            // 获取邮件对象
            Message message1 = new MimeMessage(session);
            // 设置发件人邮箱地址
            message1.setFrom(new InternetAddress(sender));
            // 设置收件人邮箱地址
            message1.setRecipient(Message.RecipientType.TO, new InternetAddress(receive));//一个收件人
            // 设置邮件标题
            message1.setSubject(title);
            // 设置邮件内容
            message1.setText(message);
            // 得到邮差对象
            Transport transport = session.getTransport();
            // 连接自己的邮箱账户
            transport.connect(sender, password);// 密码为QQ邮箱开通的stmp服务后得到的客户端授权码
            // 发送邮件
            transport.sendMessage(message1, message1.getAllRecipients());
            transport.close();
        } catch (AddressException e) {

            e.printStackTrace();
        } catch (MessagingException e) {

            e.printStackTrace();
        }
    }

}
