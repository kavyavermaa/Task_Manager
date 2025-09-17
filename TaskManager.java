import javax.swing.*;
import javax.swing.table.DefaultTableModel; // Table model for JTable
import java.awt.*; // Layout and color-related classes
import java.awt.event.*; 
import java.io.*; 
import java.time.LocalDate; 
import java.time.format.DateTimeFormatter; // For formatting date strings
import java.util.ArrayList; // For storing tasks
import java.util.Collections; // For sorting tasks by due date

public class TaskManager extends JFrame {
    private ArrayList<Task> tasks = new ArrayList<>(); // List of all tasks
    private DefaultTableModel model; // Table model to manage task data in JTable

    // GUI Components
    JTextField titleField, dateField;
    JTextArea descArea;
    JComboBox<String> priorityBox;
    JComboBox<String> filterBox;

    // Formatter for converting LocalDate to string and vice versa
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public TaskManager() {
        setTitle("Task Diary");
        setSize(1000, 600);
        setLocationRelativeTo(null); // Center the window
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(255, 240, 250));
        setLayout(new BorderLayout());

        loadTasks(); // Load saved tasks when app starts

        // TOP PANEL: Title + Filter
        JLabel titleLabel = new JLabel("Welcome to Your Personal Task Diary!", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe Script", Font.BOLD, 22));
        titleLabel.setForeground(new Color(186, 85, 211));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));

        filterBox = new JComboBox<>(new String[]{"All", "High", "Medium", "Low"});
        filterBox.setBackground(new Color(230, 200, 250));
        filterBox.addActionListener(e -> refreshTable());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("Filter by Priority: "));
        filterPanel.add(filterBox);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 240, 250));
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(filterPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // LEFT PANEL: Input Fields to Add Task
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBackground(new Color(255, 230, 240));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Write New Entry"));
        inputPanel.setPreferredSize(new Dimension(350, 500));

        titleField = new JTextField();
        descArea = new JTextArea(2, 20);
        dateField = new JTextField();
        priorityBox = new JComboBox<>(new String[]{"High", "Medium", "Low"});

        inputPanel.add(new JLabel("Title:")); inputPanel.add(titleField);
        inputPanel.add(new JLabel("Description:")); inputPanel.add(new JScrollPane(descArea));
        inputPanel.add(new JLabel("Due Date (dd-MM-yyyy):")); inputPanel.add(dateField);
        inputPanel.add(new JLabel("Priority:")); inputPanel.add(priorityBox);

        // Buttons with icons
        ImageIcon addIcon = new ImageIcon(new ImageIcon("icons/add.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        ImageIcon saveIcon = new ImageIcon(new ImageIcon("icons/save.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

        JButton addButton = new JButton(" Add", addIcon);
        addButton.setBackground(new Color(216, 191, 216));
        addButton.addActionListener(e -> addTask()); // When add button clicked

        JButton saveButton = new JButton(" Save", saveIcon);
        saveButton.setBackground(new Color(221, 160, 221));
        saveButton.addActionListener(e -> saveTasks());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(255, 230, 240));
        buttonPanel.add(addButton);
        buttonPanel.add(saveButton);

        inputPanel.add(new JLabel("")); // Left cell — EMPTY
         inputPanel.add(buttonPanel);   // Right cell — contains Add + Save buttons

        // RIGHT PANEL: Task Table View
        model = new DefaultTableModel(new String[]{"Title", "Due Date", "Priority", "Status"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
        table.setBackground(new Color(255, 250, 255));
        table.setSelectionBackground(new Color(255, 200, 255));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Your Tasks"));

        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.add(inputPanel);
        centerPanel.add(scrollPane);
        add(centerPanel, BorderLayout.CENTER);

        // BOTTOM PANEL: Buttons to Load, Toggle, Delete
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBackground(new Color(255, 230, 250));

        ImageIcon loadIcon = new ImageIcon(new ImageIcon("icons/load.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        ImageIcon toggleIcon = new ImageIcon(new ImageIcon("icons/toggle.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        ImageIcon deleteIcon = new ImageIcon(new ImageIcon("icons/delete.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));

        JButton loadButton = new JButton(" Load", loadIcon);
        loadButton.setBackground(new Color(221, 160, 221));
        loadButton.addActionListener(e -> loadTasks());

        JButton toggleButton = new JButton(" Toggle", toggleIcon);
        toggleButton.setBackground(new Color(216, 191, 216));
        toggleButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                tasks.get(row).toggleStatus(); // Mark task as complete/incomplete
                refreshTable(); // Update table view
            }
        });

        JButton deleteButton = new JButton(" Delete", deleteIcon);
        deleteButton.setBackground(new Color(255, 182, 193));
        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this task?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    tasks.remove(row); // Remove from list
                    refreshTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a task to delete.");
            }
        });

        bottomPanel.add(loadButton);
        bottomPanel.add(toggleButton);
        bottomPanel.add(deleteButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Auto-save tasks when window is closed
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveTasks();
            }
        });

        setVisible(true); // Show the window
    }

    //Adds a new task to the list
    private void addTask() {
        String title = titleField.getText().trim();
        String desc = descArea.getText().trim();
        String dateStr = dateField.getText().trim();
        String priority = (String) priorityBox.getSelectedItem();

        if (title.isEmpty() || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title and Due Date are required.");
            return;
        }

        try {
            LocalDate dueDate = LocalDate.parse(dateStr, formatter); // Parse date
            Task t = new Task(title, desc, dueDate, priority); // Create task
            tasks.add(t);
            refreshTable(); // Update view
            clearInputs(); // Reset fields
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enter date in format dd-MM-yyyy");
        }
    }

    //Refresh table data from task list
    private void refreshTable() {
        Collections.sort(tasks); // Sort by due date
        model.setRowCount(0); // Clear old rows
        LocalDate today = LocalDate.now();
        String selectedPriority = (String) filterBox.getSelectedItem();

        for (Task t : tasks) {
            if (!selectedPriority.equals("All") && !t.getPriority().equals(selectedPriority)) continue;

            String status;
            if (t.isCompleted()) {
                status = "Well Done";
            } else if (t.getDueDate().isBefore(today) || t.getDueDate().isEqual(today)) {
                status = "Due";
            } else {
                status = "Upcoming";
            }

            model.addRow(new Object[]{
                t.getTitle(),
                t.getDueDate().format(formatter),
                t.getPriority(),
                status
            });
        }
    }

    //Save tasks to file
    private void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("tasks.dat"))) {
            oos.writeObject(tasks);
            System.out.println("Tasks saved automatically.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Exception");
        }
    }

    private void loadTasks() {
        try (ObjectInputStream o = new ObjectInputStream(new FileInputStream("tasks.dat"))) {
            tasks = (ArrayList<Task>) o.readObject(); // Load tasks
            refreshTable();
            System.out.println("Tasks loaded automatically.");
        } catch (Exception e) {
            System.out.println("No saved tasks found.");
        }
    }

    //Clears input fields after adding task
    private void clearInputs() {
        titleField.setText("");
        descArea.setText("");
        dateField.setText("");
        priorityBox.setSelectedIndex(0);
    }
    public static void main(String[] args) {
        new TaskManager(); 
    }
}
