import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

// Employee class with immutability (Mutability and Immutability)
final class Employee {
    private final int id;
    private final String name;
    private final String department;

    public Employee(int id, String name, String department) {
        this.id = id;
        this.name = name;
        this.department = department;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    @Override
    public String toString() {
        return id + " - " + name + " (" + department + ")";
    }
}

// Abstract Task class (Abstract Data Type)
abstract class Task {
    protected String taskName;
    protected int duration; // in hours
    protected int hoursWorked; // Track the number of hours worked
    protected boolean completed;

    public Task(String taskName, int duration) {
        this.taskName = taskName;
        this.duration = duration;
        this.hoursWorked = 0;
        this.completed = false;
    }

    public abstract void performTask();

    public void updateTaskProgress(int hours) {
        hoursWorked += hours;
        if (hoursWorked >= duration) {
            hoursWorked = duration;
            completed = true;
        }
    }

    public String getTaskName() {
        return taskName;
    }

    public int getDuration() {
        return duration;
    }

    public int getHoursWorked() {
        return hoursWorked;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getProgress() {
        return (int) (((double) hoursWorked / duration) * 100);
    }

    @Override
    public String toString() {
        return taskName + " (Duration: " + duration + " hours, Worked: " + hoursWorked + " hours)";
    }
}

// CodingTask is a concrete implementation of Task (Abstract Data Type)
class CodingTask extends Task {
    public CodingTask(String taskName, int duration) {
        super(taskName, duration);
    }

    @Override
    public void performTask() {
        System.out.println("Performing coding task: " + taskName);
    }
}

// Generic Repository for Employee Management (Generics in Java)
class EmployeeRepository<T> { // T is a generic type, though not used here
    private final List<Employee> employees = new ArrayList<>();

    public void addEmployee(Employee employee) {
        employees.add(employee);
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public Employee findEmployeeById(int id) throws NoSuchElementException { // Exception Handling
        return employees.stream()
                .filter(e -> e.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
    }
}

// Task Manager with Synchronization (Synchronization could be added for concurrency)
class TaskManager {
    private final Map<Employee, List<Task>> taskMap = new HashMap<>();

    public void assignTask(Employee employee, Task task) {
        taskMap.computeIfAbsent(employee, k -> new ArrayList<>()).add(task);
        System.out.println("Task assigned: " + task + " to " + employee);
    }

    public List<Task> getTasksForEmployee(Employee employee) {
        return taskMap.getOrDefault(employee, Collections.emptyList());
    }

    public void updateTaskProgress(Employee employee, Task task, int hours) {
        task.updateTaskProgress(hours);
    }
}

// GUI Implementation
class GUI {
    private final EmployeeRepository<Employee> repository = new EmployeeRepository<>();
    private final TaskManager taskManager = new TaskManager();
    private final JTextArea outputArea = new JTextArea();
    private final DefaultListModel<String> employeeListModel = new DefaultListModel<>();
    private final DefaultListModel<String> taskListModel = new DefaultListModel<>();
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private JTable performanceTable;

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Employee Task Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel panel = new JPanel(new GridLayout(5, 2));

        // Employee Info
        JTextField nameField = new JTextField();
        JTextField departmentField = new JTextField();
        JButton addEmployeeButton = new JButton("Add Employee");
        JButton addTaskButton = new JButton("Assign Task");

        // Task Info
        JTextField taskNameField = new JTextField();
        JTextField taskDurationField = new JTextField();

        // Employee List
        JList<String> employeeList = new JList<>(employeeListModel);
        JScrollPane employeeScrollPane = new JScrollPane(employeeList);
        employeeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Task List and Progress
        JList<String> taskList = new JList<>(taskListModel);
        JScrollPane taskScrollPane = new JScrollPane(taskList);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setVisibleRowCount(5);

        progressBar.setStringPainted(true);

        // Performance Table
        String[] columns = {"Employee Name", "Task Name", "Progress"};
        performanceTable = new JTable(new Object[0][0], columns);
        JScrollPane performanceScrollPane = new JScrollPane(performanceTable);

        addEmployeeButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String department = departmentField.getText().trim();

            if (name.isEmpty() || department.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields!");
                return;
            }

            int id = repository.getEmployees().size() + 1;
            Employee employee = new Employee(id, name, department);
            repository.addEmployee(employee);
            employeeListModel.addElement(employee.toString());
            outputArea.append("Added Employee: " + employee + "\n");

            nameField.setText("");
            departmentField.setText("");
        });

        addTaskButton.addActionListener(e -> {
            if (employeeList.getSelectedValue() == null) {
                JOptionPane.showMessageDialog(frame, "Please select an employee first!");
                return;
            }

            String taskName = taskNameField.getText().trim();
            String durationStr = taskDurationField.getText().trim();

            if (taskName.isEmpty() || durationStr.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields!");
                return;
            }

            int duration = Integer.parseInt(durationStr);
            Employee selectedEmployee = repository.getEmployees().get(employeeList.getSelectedIndex());
            Task task = new CodingTask(taskName, duration);
            taskManager.assignTask(selectedEmployee, task);
            taskListModel.addElement(task.toString());
            updatePerformanceTable();
        });

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Department:"));
        panel.add(departmentField);
        panel.add(addEmployeeButton);
        panel.add(new JLabel("Task Name:"));
        panel.add(taskNameField);
        panel.add(new JLabel("Task Duration (hrs):"));
        panel.add(taskDurationField);
        panel.add(addTaskButton);

        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.NORTH);
        frame.add(employeeScrollPane, BorderLayout.WEST);
        frame.add(taskScrollPane, BorderLayout.CENTER);
        frame.add(progressBar, BorderLayout.SOUTH);
        frame.add(performanceScrollPane, BorderLayout.EAST);
        frame.setVisible(true);
    }

    private void updatePerformanceTable() {
        String[][] performanceData = new String[repository.getEmployees().size()][3];
        int row = 0;

        for (Employee employee : repository.getEmployees()) {
            List<Task> tasks = taskManager.getTasksForEmployee(employee);
            for (Task task : tasks) {
                performanceData[row][0] = employee.getName();
                performanceData[row][1] = task.getTaskName();
                performanceData[row][2] = task.isCompleted() ? "Completed" : task.getProgress() + "%";
                row++;
            }
        }

        performanceTable.setModel(new javax.swing.table.DefaultTableModel(
                performanceData,
                new String[]{"Employee Name", "Task Name", "Progress"}
        ));
    }
}

// Main Class
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.createAndShowGUI();
        });
    }
}
