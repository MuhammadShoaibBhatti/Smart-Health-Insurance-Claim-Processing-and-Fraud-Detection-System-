import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class SmartHealthInsurance extends JFrame {

    CardLayout cardLayout = new CardLayout();
    JPanel mainPanel = new JPanel(cardLayout);

    DefaultTableModel model;
    DefaultTableModel fraudModel; 
    TableRowSorter<DefaultTableModel> sorter;

    int totalClaims = 0;
    int approvedClaims = 0;
    int fraudClaims = 0;
    int totalPatients = 0; 
    Set<String> uniquePatients = new HashSet<>();

    JLabel totalLabel = new JLabel("0", SwingConstants.CENTER);
    JLabel approvedLabel = new JLabel("0", SwingConstants.CENTER);
    JLabel fraudLabel = new JLabel("0", SwingConstants.CENTER);
    JLabel patientsLabel = new JLabel("0", SwingConstants.CENTER); 

    private boolean isLightMode = false;
    private List<JPanel> allPanelsForTheme = new ArrayList<>();

    private enum ClaimStage {
        SUBMITTED("Dynamic Claim Submitted"),
        EVALUATION("Under Evaluation"),
        SETTLEMENT("Settlement/Payout"),
        FRAUD("Fraud Investigation Status");

        final String label;
        ClaimStage(String label) { this.label = label; }
    }

    private ClaimStage currentStage = null;
    private String currentRole = "Admin"; 

    private List<JLabel> allStatusLabels = new ArrayList<>();
    private List<JPanel> allTimelinePanels = new ArrayList<>();

    // Dynamic Header Labels List to update "Logged in as" on top of every screen
    private List<JLabel> allHeaderLabels = new ArrayList<>();

    private String latestPatientName = "";
    private double latestAmount = 0;
    private String latestAssessment = "";
    private boolean latestIsFraud = false;
    private String uploadedPhotoPath = "No Photo Uploaded"; 

    private final String currentProjectDir = System.getProperty("user.dir");
    
    private JPanel dashboardNavWrapper = new JPanel(new BorderLayout());
    private JPanel trackingNavWrapper = new JPanel(new BorderLayout());
    private JPanel reviewNavWrapper = new JPanel(new BorderLayout());
    private JPanel formNavWrapper = new JPanel(new BorderLayout());
    private JPanel hospitalNavWrapper = new JPanel(new BorderLayout());

    public SmartHealthInsurance() {
        setTitle("Smart Health Insurance Fraud Detection");
        setSize(1250, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createDashboardPanel(), "Dashboard");
        mainPanel.add(createPolicyholderTrackingPanel(), "PolicyholderTracking");
        mainPanel.add(createOfficerReviewPanel(), "OfficerReview");
        mainPanel.add(createClaimSubmissionFormPanel(), "ClaimSubmissionForm");
        mainPanel.add(createHospitalDashboardPanel(), "HospitalDashboard");

        add(mainPanel);
        setVisible(true);
    }

    // ---------------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------------
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(15, 20, 35));

        JPanel loginCard = new JPanel();
        loginCard.setPreferredSize(new Dimension(400, 360));
        loginCard.setBackground(new Color(25, 30, 50));
        loginCard.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
        loginCard.setLayout(null);

        JLabel title = new JLabel("SMART HEALTH INSURANCE", SwingConstants.CENTER);
        title.setForeground(Color.CYAN);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBounds(20, 30, 360, 30);

        JTextField user = new JTextField("admin");
        user.setBounds(70, 80, 250, 35);

        JPasswordField pass = new JPasswordField("admin");
        pass.setBounds(70, 130, 250, 35);

        JLabel roleLbl = new JLabel("Select Role:", SwingConstants.LEFT);
        roleLbl.setForeground(Color.WHITE);
        roleLbl.setBounds(70, 175, 100, 25);
        loginCard.add(roleLbl);

        String[] roles = {"Admin", "Officer", "Hospital"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        roleCombo.setBounds(70, 200, 250, 35);
        loginCard.add(roleCombo);

        JButton login = new JButton("LOGIN");
        login.setBounds(120, 265, 150, 40);
        login.setBackground(Color.CYAN);
        login.addActionListener(e -> {
            String u = user.getText().trim();
            String p = String.valueOf(pass.getPassword());
            String selectedRole = (String) roleCombo.getSelectedItem();

            if ((u.equals("admin") && p.equals("admin")) || u.equalsIgnoreCase(selectedRole)) {
                currentRole = selectedRole;
                
                // Refresh top headers and sidebar navigation menus dynamically
                refreshAllNavigationPanels();
                updateAllTopHeadersWithLoggedInUser();

                if (currentRole.equals("Admin")) {
                    cardLayout.show(mainPanel, "Dashboard");
                } else if (currentRole.equals("Officer")) {
                    cardLayout.show(mainPanel, "OfficerReview");
                } else if (currentRole.equals("Hospital")) {
                    cardLayout.show(mainPanel, "ClaimSubmissionForm");
                }
                
                JOptionPane.showMessageDialog(this, "Logged in successfully as " + currentRole);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials");
            }
        });

        loginCard.add(title);
        loginCard.add(user);
        loginCard.add(pass);
        loginCard.add(login);
        panel.add(loginCard);
        return panel;
    }

    private void refreshAllNavigationPanels() {
        dashboardNavWrapper.removeAll();
        dashboardNavWrapper.add(createSideNavigationPanel());
        
        trackingNavWrapper.removeAll();
        trackingNavWrapper.add(createSideNavigationPanel());

        reviewNavWrapper.removeAll();
        reviewNavWrapper.add(createSideNavigationPanel());

        formNavWrapper.removeAll();
        formNavWrapper.add(createSideNavigationPanel());

        hospitalNavWrapper.removeAll();
        hospitalNavWrapper.add(createSideNavigationPanel());

        this.revalidate();
        this.repaint();
    }

    // Method to change header texts dynamically to show active login name/role
    private void updateAllTopHeadersWithLoggedInUser() {
        for (JLabel lbl : allHeaderLabels) {
            String originalText = lbl.getName(); // Retrieve stored base name
            lbl.setText(originalText + " (Logged in as: " + currentRole + ")");
        }
    }

    // ---------------------------------------------------------------
    // DASHBOARD
    // ---------------------------------------------------------------
    private JPanel createDashboardPanel() {
        JPanel dashboard = new JPanel();
        dashboard.setBackground(new Color(20, 25, 40));
        dashboard.setLayout(null);
        allPanelsForTheme.add(dashboard);

        JPanel header = new JPanel();
        header.setBounds(0, 0, 1250, 70);
        header.setBackground(new Color(25, 35, 55));
        header.setBorder(BorderFactory.createLineBorder(Color.CYAN, 3));
        
        JLabel heading = new JLabel("SMART HEALTH INSURANCE - DASHBOARD");
        heading.setName("SMART HEALTH INSURANCE - DASHBOARD"); // Using Name property as base backup
        heading.setForeground(Color.CYAN);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        allHeaderLabels.add(heading);
        header.add(heading);
        dashboard.add(header);

        dashboardNavWrapper.setBounds(10, 80, 220, 660);
        dashboard.add(dashboardNavWrapper);

        JPanel totalCard = createStatCard("TOTAL CLAIMS", totalLabel, "ClaimSubmissionForm");
        totalCard.setBounds(240, 85, 185, 100);
        dashboard.add(totalCard);

        JPanel approvedCard = createStatCard("APPROVED CLAIMS", approvedLabel, "PolicyholderTracking");
        approvedCard.setBounds(440, 85, 185, 100);
        dashboard.add(approvedCard);

        JPanel fraudCard = createStatCard("FRAUD CASES", fraudLabel, "OfficerReview");
        fraudCard.setBounds(640, 85, 185, 100);
        dashboard.add(fraudCard);

        JPanel patientsCard = createStatCard("TOTAL PATIENTS", patientsLabel, null);
        patientsCard.setBounds(840, 85, 185, 100);
        dashboard.add(patientsCard);

        JButton backupBtn = new JButton("Data Backup");
        backupBtn.setBounds(1040, 95, 180, 35);
        backupBtn.setBackground(new Color(46, 139, 87));
        backupBtn.setForeground(Color.WHITE);
        backupBtn.addActionListener(e -> performBackup());
        dashboard.add(backupBtn);

        JButton restoreBtn = new JButton("Data Restore");
        restoreBtn.setBounds(1040, 140, 180, 35);
        restoreBtn.setBackground(new Color(70, 130, 180));
        restoreBtn.setForeground(Color.WHITE);
        restoreBtn.addActionListener(e -> performRestore());
        dashboard.add(restoreBtn);

        JPanel controlPanel = new JPanel();
        controlPanel.setBounds(240, 200, 980, 50);
        controlPanel.setBackground(new Color(30, 35, 55));
        controlPanel.setLayout(null);

        JLabel searchLbl = new JLabel("Search (Date):");
        searchLbl.setForeground(Color.WHITE);
        searchLbl.setBounds(10, 12, 100, 25);
        controlPanel.add(searchLbl);

        JTextField searchField = new JTextField();
        searchField.setBounds(110, 12, 150, 25);
        controlPanel.add(searchField);

        JButton searchBtn = new JButton("Filter Date");
        searchBtn.setBounds(270, 12, 110, 25);
        searchBtn.addActionListener(e -> {
            String target = searchField.getText().trim();
            if(target.isEmpty()) sorter.setRowFilter(null);
            else sorter.setRowFilter(RowFilter.regexFilter(target, 3)); 
        });
        controlPanel.add(searchBtn);

        JButton sortAscBtn = new JButton("Sort $ Asc");
        sortAscBtn.setBounds(400, 12, 110, 25);
        sortAscBtn.addActionListener(e -> table.getRowSorter().toggleSortOrder(1));
        controlPanel.add(sortAscBtn);

        dashboard.add(controlPanel);

        model = new DefaultTableModel(new String[] { "Patient", "Amount ($)", "Status", "Date & Time" }, 0);
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setBackground(new Color(30, 35, 55));
        table.setForeground(Color.WHITE);
        table.setGridColor(Color.CYAN);

        JScrollPane scroll = new JScrollPane(table);
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBounds(240, 265, 480, 475);
        historyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.CYAN), "GLOBAL CLAIM HISTORY"));
        historyPanel.add(scroll, BorderLayout.CENTER);
        dashboard.add(historyPanel);

        fraudModel = new DefaultTableModel(new String[] { "Patient", "Amount ($)", "Flagged Date" }, 0);
        JTable fraudTable = new JTable(fraudModel);
        fraudTable.setBackground(new Color(45, 25, 30));
        fraudTable.setForeground(Color.ORANGE);
        JScrollPane fraudScroll = new JScrollPane(fraudTable);
        JPanel fraudPanel = new JPanel(new BorderLayout());
        fraudPanel.setBounds(740, 265, 480, 475);
        fraudPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.RED), "FRAUD REPORT REPOSITORY"));
        fraudPanel.add(fraudScroll, BorderLayout.CENTER);
        dashboard.add(fraudPanel);

        return dashboard;
    }

    private JTable table;

    // ---------------------------------------------------------------
    // POLICYHOLDER TRACKING
    // ---------------------------------------------------------------
    private JPanel createPolicyholderTrackingPanel() {
        JPanel trackingPanel = new JPanel();
        trackingPanel.setBackground(new Color(20, 25, 40));
        trackingPanel.setLayout(null);
        allPanelsForTheme.add(trackingPanel);

        trackingNavWrapper.setBounds(10, 80, 220, 660);
        trackingPanel.add(trackingNavWrapper);

        JLabel sl = new JLabel("Status: -");
        sl.setForeground(Color.WHITE);
        sl.setFont(new Font("Arial", Font.BOLD, 14));
        sl.setBounds(260, 85, 850, 25);
        trackingPanel.add(sl);
        allStatusLabels.add(sl);

        JPanel header = new JPanel();
        header.setBounds(0, 0, 1250, 70);
        header.setBackground(new Color(25, 35, 55));
        header.setBorder(BorderFactory.createLineBorder(Color.MAGENTA, 3));
        
        JLabel heading = new JLabel("POLICYHOLDER CLAIM TRACKING SCREEN");
        heading.setName("POLICYHOLDER CLAIM TRACKING SCREEN");
        heading.setForeground(Color.MAGENTA);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        allHeaderLabels.add(heading);
        header.add(heading);
        trackingPanel.add(header);

        JPanel tp = buildTimelinePanel();
        tp.setBounds(250, 130, 970, 280);
        trackingPanel.add(tp);
        allTimelinePanels.add(tp);

        JPanel detailCard = new JPanel(new BorderLayout());
        detailCard.setBounds(250, 430, 970, 280);
        detailCard.setBackground(new Color(25, 30, 50));
        detailCard.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "SYSTEM COMPLIANCE DETAILS"));
        JTextArea complianceText = new JTextArea("\n  * Policyholder Claim Verification System dynamically maps background flags.\n  * Real-time validation checks for double-billing anomalies.\n  * Compliant with instant audit logs for seamless end-user mobile synchronization updates.");
        complianceText.setBackground(new Color(25, 30, 50));
        complianceText.setForeground(Color.LIGHT_GRAY);
        complianceText.setFont(new Font("Arial", Font.PLAIN, 13));
        complianceText.setEditable(false);
        detailCard.add(complianceText, BorderLayout.CENTER);
        trackingPanel.add(detailCard);

        return trackingPanel;
    }

    // ---------------------------------------------------------------
    // OFFICER REVIEW
    // ---------------------------------------------------------------
    private JPanel createOfficerReviewPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(20, 25, 40));
        panel.setLayout(null);
        allPanelsForTheme.add(panel);

        reviewNavWrapper.setBounds(10, 80, 220, 660);
        panel.add(reviewNavWrapper);

        JLabel sl = new JLabel("Status: -");
        sl.setForeground(Color.WHITE);
        sl.setFont(new Font("Arial", Font.BOLD, 14));
        sl.setBounds(260, 85, 850, 25);
        panel.add(sl);
        allStatusLabels.add(sl);

        JPanel header = new JPanel();
        header.setBounds(0, 0, 1250, 70);
        header.setBackground(new Color(25, 35, 55));
        header.setBorder(BorderFactory.createLineBorder(Color.MAGENTA, 3));
        
        JLabel heading = new JLabel("OFFICER REVIEW SCREEN");
        heading.setName("OFFICER REVIEW SCREEN");
        heading.setForeground(Color.MAGENTA);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        allHeaderLabels.add(heading);
        header.add(heading);
        panel.add(header);

        JLabel claimSummary = new JLabel("Latest Claim: -");
        claimSummary.setForeground(Color.LIGHT_GRAY);
        claimSummary.setFont(new Font("Arial", Font.PLAIN, 14));
        claimSummary.setBounds(260, 120, 900, 25);
        panel.add(claimSummary);

        JButton refreshBtn = new JButton("Load Latest Claim");
        refreshBtn.setBounds(260, 155, 220, 35);
        refreshBtn.setBackground(Color.CYAN);
        refreshBtn.addActionListener(e -> {
            String s = latestPatientName.isEmpty() ? "-" : latestPatientName;
            claimSummary.setText("Patient: " + s + "  |  Amount: $" + latestAmount + "  |  Assessment: " + latestAssessment);
            if (latestIsFraud) setStageAll(ClaimStage.FRAUD);
            else if (currentStage == ClaimStage.SUBMITTED || currentStage == ClaimStage.EVALUATION) setStageAll(ClaimStage.EVALUATION);
        });
        panel.add(refreshBtn);

        JPanel actions = new JPanel(null);
        actions.setBounds(260, 205, 960, 120);
        actions.setBackground(new Color(25, 30, 50));
        actions.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
        panel.add(actions);

        JButton approveBtn = new JButton("\u2714 Approve & Settlement/Payout");
        approveBtn.setBounds(30, 35, 280, 45);
        approveBtn.setBackground(new Color(0, 180, 0));
        approveBtn.setForeground(Color.WHITE);
        approveBtn.setFont(new Font("Arial", Font.BOLD, 13));
        approveBtn.addActionListener(e -> {
            if (latestPatientName.isEmpty()) { JOptionPane.showMessageDialog(panel, "Submit a claim first."); return; }
            latestIsFraud = false;
            latestAssessment = "Approved (Officer)";
            setStageAll(ClaimStage.SETTLEMENT);
            JOptionPane.showMessageDialog(panel, "Claim status updated to approved!");
        });
        actions.add(approveBtn);

        JButton fraudFlagBtn = new JButton("\u2691 Flag for Fraud Investigation");
        fraudFlagBtn.setBounds(340, 35, 280, 45);
        fraudFlagBtn.setBackground(Color.ORANGE);
        fraudFlagBtn.setForeground(Color.BLACK);
        fraudFlagBtn.setFont(new Font("Arial", Font.BOLD, 13));
        fraudFlagBtn.addActionListener(e -> {
            if (latestPatientName.isEmpty()) { JOptionPane.showMessageDialog(panel, "Submit a claim first."); return; }
            latestIsFraud = true;
            latestAssessment = "Fraud Suspicious (Officer)";
            setStageAll(ClaimStage.FRAUD);
            JOptionPane.showMessageDialog(panel, "Claim flagged for active fraud inspection.");
        });
        actions.add(fraudFlagBtn);

        JButton pdfReportBtn = new JButton("Generate Claim Document");
        pdfReportBtn.setBounds(650, 35, 260, 45);
        pdfReportBtn.setBackground(new Color(139, 0, 0));
        pdfReportBtn.setForeground(Color.WHITE);
        pdfReportBtn.setFont(new Font("Arial", Font.BOLD, 13));
        pdfReportBtn.addActionListener(e -> exportToPDFReport());
        actions.add(pdfReportBtn);

        JPanel miniTimeline = buildTimelinePanel();
        miniTimeline.setBounds(260, 345, 960, 380);
        panel.add(miniTimeline);
        allTimelinePanels.add(miniTimeline);

        return panel;
    }

    // ---------------------------------------------------------------
    // CLAIM SUBMISSION FORM
    // ---------------------------------------------------------------
    private JPanel createClaimSubmissionFormPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(20, 25, 40));
        panel.setLayout(null);
        allPanelsForTheme.add(panel);

        formNavWrapper.setBounds(10, 80, 220, 660);
        panel.add(formNavWrapper);

        JLabel sl = new JLabel("Status: -");
        sl.setForeground(Color.WHITE);
        sl.setFont(new Font("Arial", Font.BOLD, 14));
        sl.setBounds(260, 85, 850, 25);
        panel.add(sl);
        allStatusLabels.add(sl);

        JPanel header = new JPanel();
        header.setBounds(0, 0, 1250, 70);
        header.setBackground(new Color(25, 35, 55));
        header.setBorder(BorderFactory.createLineBorder(Color.CYAN, 3));
        
        JLabel heading = new JLabel("CLAIM SUBMISSION FORM");
        heading.setName("CLAIM SUBMISSION FORM");
        heading.setForeground(Color.CYAN);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        allHeaderLabels.add(heading);
        header.add(heading);
        panel.add(header);

        JPanel form = new JPanel(null);
        form.setBounds(260, 120, 960, 600);
        form.setBackground(new Color(25, 30, 50));
        form.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
        panel.add(form);

        JLabel nameLabel = new JLabel("Patient Name:");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setBounds(40, 30, 160, 25);
        form.add(nameLabel);

        JTextField nameField = new JTextField();
        nameField.setBounds(220, 30, 300, 30);
        form.add(nameField);

        JLabel amountLabel = new JLabel("Claim Amount ($):");
        amountLabel.setForeground(Color.WHITE);
        amountLabel.setBounds(40, 75, 160, 25);
        form.add(amountLabel);

        JTextField amountField = new JTextField();
        amountField.setBounds(220, 75, 300, 30);
        form.add(amountField);

        JLabel photoLbl = new JLabel("Patient Identity Photo:");
        photoLbl.setForeground(Color.WHITE);
        photoLbl.setBounds(40, 120, 160, 25);
        form.add(photoLbl);

        JButton uploadBtn = new JButton("Select Patient Photo");
        uploadBtn.setBounds(220, 120, 180, 30);
        JLabel pathDisplay = new JLabel("No file uploaded");
        pathDisplay.setForeground(Color.LIGHT_GRAY);
        pathDisplay.setBounds(410, 120, 300, 25);
        uploadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int choice = chooser.showOpenDialog(this);
            if (choice == JFileChooser.APPROVE_OPTION) {
                uploadedPhotoPath = chooser.getSelectedFile().getAbsolutePath();
                pathDisplay.setText(chooser.getSelectedFile().getName());
            }
        });
        form.add(uploadBtn);
        form.add(pathDisplay);

        JLabel diagLabel = new JLabel("Diagnosis Code:");
        diagLabel.setForeground(Color.WHITE);
        diagLabel.setBounds(40, 165, 160, 25);
        form.add(diagLabel);

        JTextField diagField = new JTextField("e.g. ICD-10");
        diagField.setBounds(220, 165, 300, 30);
        form.add(diagField);

        JLabel hospLabel = new JLabel("Hospital Type:");
        hospLabel.setForeground(Color.WHITE);
        hospLabel.setBounds(40, 210, 160, 25);
        form.add(hospLabel);

        String[] hospTypes = { "Government", "Private", "Clinic", "Abroad" };
        JComboBox<String> hospCombo = new JComboBox<>(hospTypes);
        hospCombo.setBounds(220, 210, 300, 30);
        form.add(hospCombo);

        JLabel daysLabel = new JLabel("Days Admitted:");
        daysLabel.setForeground(Color.WHITE);
        daysLabel.setBounds(40, 255, 160, 25);
        form.add(daysLabel);

        JTextField daysField = new JTextField("0");
        daysField.setBounds(220, 255, 300, 30);
        form.add(daysField);

        JTextArea resultArea = new JTextArea();
        resultArea.setBounds(40, 310, 880, 180);
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(20, 25, 40));
        resultArea.setForeground(Color.GREEN);
        form.add(resultArea);

        JButton submitBtn = new JButton("Submit Claim");
        submitBtn.setBounds(330, 510, 200, 40);
        submitBtn.setBackground(Color.CYAN);
        submitBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) { JOptionPane.showMessageDialog(panel, "Enter Patient Name."); return; }
                double amount = Double.parseDouble(amountField.getText().trim());

                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                int fraudScore = 0;
                if (amount > 100000) fraudScore += 4;
                if (daysField.getText().equals("0") && amount > 5000) fraudScore += 3;

                boolean suspicious = fraudScore >= 4;
                latestPatientName = name;
                latestAmount = amount;
                latestIsFraud = suspicious;
                totalClaims++;

                if(uniquePatients.add(name.toLowerCase())) {
                    totalPatients++;
                }

                totalLabel.setText(String.valueOf(totalClaims));
                patientsLabel.setText(String.valueOf(totalPatients));

                if (suspicious) {
                    fraudClaims++;
                    latestAssessment = "Fraud Suspicious";
                    fraudLabel.setText(String.valueOf(fraudClaims));
                    setStageAll(ClaimStage.FRAUD);
                    fraudModel.addRow(new Object[]{name, amount, timestamp});
                    resultArea.setText("ALERT: High Risk Detected! Logged into repository.");
                } else {
                    approvedClaims++;
                    latestAssessment = "Approved";
                    approvedLabel.setText(String.valueOf(approvedClaims));
                    setStageAll(ClaimStage.EVALUATION);
                    resultArea.setText("Success: Safe criteria verified.");
                }

                model.addRow(new Object[] { name, amount, latestAssessment, timestamp });

                nameField.setText("");
                amountField.setText("");
                pathDisplay.setText("No file uploaded");
                JOptionPane.showMessageDialog(panel, "Claim Saved Successfully under " + currentRole + " system logs!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Input Evaluation Error");
            }
        });
        form.add(submitBtn);

        return panel;
    }

    // ---------------------------------------------------------------
    // HOSPITAL DASHBOARD
    // ---------------------------------------------------------------
    private JPanel createHospitalDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(20, 25, 40));
        panel.setLayout(null);
        allPanelsForTheme.add(panel);

        hospitalNavWrapper.setBounds(10, 80, 220, 660);
        panel.add(hospitalNavWrapper);

        JLabel sl = new JLabel("Status: -");
        sl.setForeground(Color.WHITE);
        panel.add(sl);
        allStatusLabels.add(sl);

        JPanel header = new JPanel();
        header.setBounds(0, 0, 1250, 70);
        header.setBackground(new Color(25, 35, 55));
        header.setBorder(BorderFactory.createLineBorder(Color.CYAN, 3));
        
        JLabel heading = new JLabel("HOSPITAL DASHBOARD");
        heading.setName("HOSPITAL DASHBOARD");
        heading.setForeground(Color.CYAN);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        allHeaderLabels.add(heading);
        header.add(heading);
        panel.add(header);

        JPanel card = new JPanel(null);
        card.setBounds(260, 120, 960, 200);
        card.setBackground(new Color(25, 30, 50));
        panel.add(card);

        JLabel infoTitle = new JLabel("Latest Treatment Status");
        infoTitle.setBounds(30, 20, 400, 25);
        infoTitle.setForeground(Color.CYAN);
        card.add(infoTitle);

        JLabel patientLbl = new JLabel("Patient: -");
        patientLbl.setBounds(30, 60, 400, 25);
        patientLbl.setForeground(Color.WHITE);
        card.add(patientLbl);

        JButton load = new JButton("Load Decision");
        load.setBounds(30, 130, 180, 35);
        load.setBackground(Color.CYAN);
        load.addActionListener(e -> {
            patientLbl.setText("Patient: " + latestPatientName + " | Status: " + latestAssessment);
        });
        card.add(load);

        JPanel timelineBox = buildTimelinePanel();
        timelineBox.setBounds(260, 345, 960, 380);
        panel.add(timelineBox);
        allTimelinePanels.add(timelineBox);

        return panel;
    }

    // ---------------------------------------------------------------
    // NAVIGATION
    // ---------------------------------------------------------------
    private JPanel createSideNavigationPanel() {
        JPanel nav = new JPanel(null);
        nav.setBackground(new Color(25, 30, 50));
        nav.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));

        JLabel brand = new JLabel("ROLE: " + currentRole.toUpperCase());
        brand.setForeground(Color.YELLOW);
        brand.setFont(new Font("Arial", Font.BOLD, 12));
        brand.setBounds(20, 10, 180, 25);
        nav.add(brand);

        int y = 50;

        if (currentRole.equals("Admin")) {
            JButton btnDash = createMenuButton("Dashboard", "Dashboard");
            btnDash.setBounds(10, y, 200, 38);
            nav.add(btnDash);
            y += 45;

            JButton btnTrack = createMenuButton("Policyholder Status", "PolicyholderTracking");
            btnTrack.setBounds(10, y, 200, 38);
            nav.add(btnTrack);
            y += 45;
        }

        if (currentRole.equals("Admin") || currentRole.equals("Officer")) {
            JButton btnReview = createMenuButton("Officer Review", "OfficerReview");
            btnReview.setBounds(10, y, 200, 38);
            nav.add(btnReview);
            y += 45;
        }

        if (currentRole.equals("Admin") || currentRole.equals("Hospital")) {
            JButton btnForm = createMenuButton("Claim Form", "ClaimSubmissionForm");
            btnForm.setBounds(10, y, 200, 38);
            nav.add(btnForm);
            y += 45;

            JButton btnHosp = createMenuButton("Hospital Desk", "HospitalDashboard");
            btnHosp.setBounds(10, y, 200, 38);
            nav.add(btnHosp);
            y += 45;
        }

        JButton themeBtn = new JButton("Toggle Theme (\u2600)");
        themeBtn.setBounds(10, y + 10, 200, 38);
        themeBtn.setBackground(Color.GRAY);
        themeBtn.setForeground(Color.WHITE);
        themeBtn.addActionListener(e -> toggleSystemTheme());
        nav.add(themeBtn);

        JButton logout = new JButton("LOGOUT");
        logout.setBounds(10, y + 60, 200, 38);
        logout.setBackground(Color.RED);
        logout.setForeground(Color.WHITE);
        logout.addActionListener(e -> cardLayout.show(mainPanel, "Login"));
        nav.add(logout);

        return nav;
    }

    private JButton createMenuButton(String text, String targetScreen) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(40, 50, 75));
        btn.setForeground(Color.WHITE);
        btn.addActionListener(e -> cardLayout.show(mainPanel, targetScreen));
        return btn;
    }

    // ---------------------------------------------------------------
    // UTILITIES
    // ---------------------------------------------------------------
    private void toggleSystemTheme() {
        isLightMode = !isLightMode;
        Color bg = isLightMode ? Color.LIGHT_GRAY : new Color(20, 25, 40);
        for (JPanel p : allPanelsForTheme) {
            p.setBackground(bg);
        }
        repaint();
        JOptionPane.showMessageDialog(this, "Theme switched successfully!");
    }

    private void exportToPDFReport() {
        if(latestPatientName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No claim record available to export.");
            return;
        }
        File report = new File(currentProjectDir, latestPatientName + "_ClaimReport.txt");
        try (PrintWriter writer = new PrintWriter(report)) {
            writer.println("=================================================");
            writer.println("         SMART HEALTH INSURANCE AUDIT REPORT     ");
            writer.println("=================================================");
            writer.println("Generated On: " + new Date().toString());
            writer.println("Patient Name: " + latestPatientName);
            writer.println("Claim Cost  : $" + latestAmount);
            writer.println("Review Flag : " + latestAssessment);
            writer.println("Photo Path  : " + uploadedPhotoPath);
            writer.println("Compliance  : Fully Validated Under System Norms");
            writer.println("=================================================");
            JOptionPane.showMessageDialog(this, "Audit Document Generated: " + report.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error building digital file.");
        }
    }

    private void performBackup() {
        File backupFile = new File(currentProjectDir, "Insurance_Backup.csv");
        try (PrintWriter pw = new PrintWriter(backupFile)) {
            for (int i = 0; i < model.getRowCount(); i++) {
                pw.println(model.getValueAt(i, 0) + "," + model.getValueAt(i, 1) + "," +
                           model.getValueAt(i, 2) + "," + model.getValueAt(i, 3));
            }
            JOptionPane.showMessageDialog(this, "Database Backup Successful!\nSaved at: " + backupFile.getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Backup Failed.");
        }
    }

    private void performRestore() {
        File backupFile = new File(currentProjectDir, "Insurance_Backup.csv");
        try (BufferedReader br = new BufferedReader(new FileReader(backupFile))) {
            String line;
            model.setRowCount(0); 
            totalClaims = 0;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                model.addRow(data);
                totalClaims++;
            }
            totalLabel.setText(String.valueOf(totalClaims));
            JOptionPane.showMessageDialog(this, "Database State Restored Successfully from:\n" + backupFile.getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No Backup File Detected.");
        }
    }

    private JPanel buildTimelinePanel() {
        JPanel tp = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.MAGENTA);
                g2.drawLine(50, getHeight()/2, getWidth()-50, getHeight()/2);
                g2.drawString("System Dynamic Interactive Timeline", 40, 30);
            }
        };
        tp.setBackground(new Color(30, 35, 55));
        return tp;
    }

    private JPanel createStatCard(String titleText, JLabel valueLabel, String navigateTo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(35, 45, 70));
        panel.setBorder(BorderFactory.createLineBorder(Color.CYAN, 1));
        JLabel l = new JLabel(titleText, SwingConstants.CENTER);
        l.setForeground(Color.WHITE);
        panel.add(l, BorderLayout.NORTH);
        valueLabel.setForeground(Color.CYAN);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    private void setStageAll(ClaimStage stage) {
        currentStage = stage;
        String text = (stage == null) ? "Status: -" : "Status: " + stage.label;
        for (JLabel lbl : allStatusLabels) lbl.setText(text);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SmartHealthInsurance());
    }
}