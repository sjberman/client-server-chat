package client;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import javax.swing.JTextPane;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class LoginGUI extends JFrame {

	private JPanel LoginFrame;
    private JPanel loginInputPanel;
    private JLabel loginLabel;
    private JPanel loginOptionalPanel;
	private JTextField Username;
	private JTextField Password;
	private JTextField txtHostoptional;
	private JTextField txtPortoptional;
	private JLabel UserIdLabel;
	private JLabel PasswordLabel;
	private JLabel HostLabel;
	private JLabel PortLabel;   
    private JButton LogInButton;

	public LoginGUI(Boolean error) {
		LoginFrame = new JPanel();
        LogInButton = new JButton();
        loginLabel = new JLabel();
        loginInputPanel = new JPanel();
        PasswordLabel = new JLabel();
        UserIdLabel = new JLabel();
        Username = new JTextField();
        Password = new JTextField();
        loginOptionalPanel = new javax.swing.JPanel();
        txtPortoptional = new JTextField();
        PortLabel = new JLabel();
        HostLabel = new JLabel();
        txtHostoptional = new JTextField();
        
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		LoginFrame.setBackground(new java.awt.Color(52, 73, 94));

        LogInButton.setText("Log In");

        loginLabel.setFont(new java.awt.Font("Vijaya", 1, 24)); // NOI18N
        loginLabel.setForeground(new java.awt.Color(240, 240, 240));
        loginLabel.setText("SimpleChat");

        loginInputPanel.setBackground(new java.awt.Color(52, 73, 94));
        loginInputPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        PasswordLabel.setForeground(new java.awt.Color(240, 240, 240));
        PasswordLabel.setText("Password:");

        UserIdLabel.setForeground(new java.awt.Color(240, 240, 240));
        UserIdLabel.setText("User Id:");

        Username.setBackground(new java.awt.Color(127, 140, 141));
        Password.setBackground(new java.awt.Color(127, 140, 141));

        javax.swing.GroupLayout loginInputPanelLayout = new javax.swing.GroupLayout(loginInputPanel);
        loginInputPanel.setLayout(loginInputPanelLayout);
        loginInputPanelLayout.setHorizontalGroup(
            loginInputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginInputPanelLayout.createSequentialGroup()
                .addGroup(loginInputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(loginInputPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(UserIdLabel))
                    .addGroup(loginInputPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(PasswordLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(loginInputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(Username, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                    .addComponent(Password))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        loginInputPanelLayout.setVerticalGroup(
            loginInputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, loginInputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(loginInputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(UserIdLabel)
                    .addComponent(Username, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(loginInputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PasswordLabel)
                    .addComponent(Password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        loginOptionalPanel.setBackground(new java.awt.Color(52, 73, 94));
        loginOptionalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), "Optional", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.BELOW_TOP, null, new java.awt.Color(240, 240, 240)));
        loginOptionalPanel.setForeground(new java.awt.Color(240, 240, 240));

        txtPortoptional.setBackground(new java.awt.Color(127, 140, 141));

        PortLabel.setForeground(new java.awt.Color(240, 240, 240));
        PortLabel.setText("Port:");

        HostLabel.setForeground(new java.awt.Color(240, 240, 240));
        HostLabel.setText("Host:");

        txtHostoptional.setBackground(new java.awt.Color(127, 140, 141));

        javax.swing.GroupLayout loginOptionalPanelLayout = new javax.swing.GroupLayout(loginOptionalPanel);
        loginOptionalPanel.setLayout(loginOptionalPanelLayout);
        loginOptionalPanelLayout.setHorizontalGroup(
            loginOptionalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginOptionalPanelLayout.createSequentialGroup()
                .addContainerGap(26, Short.MAX_VALUE)
                .addGroup(loginOptionalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(HostLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(PortLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(loginOptionalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtHostoptional, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPortoptional, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );
        loginOptionalPanelLayout.setVerticalGroup(
            loginOptionalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, loginOptionalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(loginOptionalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(HostLabel)
                    .addComponent(txtHostoptional, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(loginOptionalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PortLabel)
                    .addComponent(txtPortoptional, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout LoginFrameLayout = new javax.swing.GroupLayout(LoginFrame);
        LoginFrame.setLayout(LoginFrameLayout);
        LoginFrameLayout.setHorizontalGroup(
            LoginFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LoginFrameLayout.createSequentialGroup()
                .addGap(99, 99, 99)
                .addComponent(LogInButton, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(LoginFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(LoginFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(loginOptionalPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loginInputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LoginFrameLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(loginLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(87, 87, 87))
        );
        LoginFrameLayout.setVerticalGroup(
            LoginFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LoginFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(loginLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loginInputPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(loginOptionalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(LogInButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(LoginFrame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(LoginFrame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        setContentPane(LoginFrame);
		
        if(error){
        	Username.setBackground(Color.RED);
        	Password.setBackground(Color.RED);
        }
        
        LogInButton.setBackground(Color.decode("#1ABC9C"));
		
		LogInButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String command = LogInButton.getActionCommand();
				if(command == "Log In"){
					//get new channel
					String id = Username.getText();
					String pw = Password.getText();
					String host = txtHostoptional.getText();
					String port = txtPortoptional.getText();
					int portNumber;
					if(id.length() < 1 || pw.length() < 1) {
						Username.setText("");
						Password.setText("");
						Username.setBackground(Color.RED);
						Password.setBackground(Color.RED);
					}else {
						if (host.length() == 0) {
							host = "localhost";
						}
						if (port.length() == 0 || port.equals("DEFAULT_PORT")) {
							portNumber = 5555;
						}else {
							portNumber = Integer.parseInt(port);
						}
						ClientGUI client = new ClientGUI(id, pw, host, portNumber);
						setVisible(false);
					}
				}
			}
		});

		pack();
		setVisible(true);
	}
}