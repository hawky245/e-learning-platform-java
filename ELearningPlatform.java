// ELearningPlatform.java.java
// Compile: javac ELearningPlatform.java.java
// Run:     java ELearningPlatform
//
// Minimal Swing GUI using JList and dialogs. No persistence, single-file.

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ELearningPlatform {
    /* --- Models --- */
    static class Student { long id; String name, email; Student(long id, String n, String e){this.id=id;name=n;email=e;} public String toString(){return id+" - "+name;} }
    static class Course  { long id; String code, title; int credits; Course(long id,String c,String t,int cr){this.id=id;code=c;title=t;credits=cr;} public String toString(){return id+" - "+code+" : "+title;} }
    static class Enrollment { long id; Student s; Course c; Grade g; Enrollment(long id,Student s,Course c){this.id=id;this.s=s;this.c=c;} public String toString(){return "E"+id+" | "+s.name+" -> "+c.code+" | "+(g==null? "-" : g); } }
    enum Grade { A,B,C,D,F,P,NP }

    /* --- Data --- */
    private final List<Student> students = new ArrayList<>();
    private final List<Course> courses = new ArrayList<>();
    private final List<Enrollment> enrolls = new ArrayList<>();
    private long sid = 1, cid = 1, eid = 1;

    /* --- UI --- */
    private final DefaultListModel<Student> studentModel = new DefaultListModel<>();
    private final DefaultListModel<Course> courseModel = new DefaultListModel<>();
    private final DefaultListModel<Enrollment> enrollModel = new DefaultListModel<>();

    private void seed() {
        Student a = new Student(sid++,"Alice","alice@example.com");
        Student b = new Student(sid++,"Bob","bob@example.com");
        students.add(a); students.add(b); studentModel.addElement(a); studentModel.addElement(b);

        Course c1 = new Course(cid++,"CS101","Intro to CS",4);
        Course c2 = new Course(cid++,"MA101","Calculs I",3);
        courses.add(c1); courses.add(c2); courseModel.addElement(c1); courseModel.addElement(c2);

        enrolls.add(newEnrollment(a,c1,Grade.A));
        enrolls.add(newEnrollment(b,c1,null));
    }

    private Enrollment newEnrollment(Student s, Course c, Grade g){
        Enrollment e = new Enrollment(eid++, s, c);
        e.g = g;
        enrollModel.addElement(e);
        return e;
    }

    private void buildAndShowGui(){
        JFrame f = new JFrame("E-Learning — Simple");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(760,420);
        f.setLocationRelativeTo(null);

        // left: students list and controls
        JList<Student> sList = new JList<>(studentModel);
        JList<Course>  cList = new JList<>(courseModel);
        JList<Enrollment> eList = new JList<>(enrollModel);

        JPanel left = new JPanel(new BorderLayout(6,6));
        left.add(new JLabel("Students"), BorderLayout.NORTH);
        left.add(new JScrollPane(sList), BorderLayout.CENTER);
        JPanel sBtns = new JPanel(new GridLayout(1,3,6,6));
        JButton addS = new JButton("Add"); JButton delS = new JButton("Delete"); JButton viewT = new JButton("Transcript");
        sBtns.add(addS); sBtns.add(delS); sBtns.add(viewT);
        left.add(sBtns, BorderLayout.SOUTH);

        JPanel mid = new JPanel(new BorderLayout(6,6));
        mid.add(new JLabel("Courses"), BorderLayout.NORTH);
        mid.add(new JScrollPane(cList), BorderLayout.CENTER);
        JPanel cBtns = new JPanel(new GridLayout(1,3,6,6));
        JButton addC = new JButton("Add"); JButton delC = new JButton("Delete"); JButton enrollBtn = new JButton("Enroll ->");
        cBtns.add(addC); cBtns.add(delC); cBtns.add(enrollBtn);
        mid.add(cBtns, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout(6,6));
        right.add(new JLabel("Enrollments"), BorderLayout.NORTH);
        right.add(new JScrollPane(eList), BorderLayout.CENTER);
        JPanel eBtns = new JPanel(new GridLayout(1,3,6,6));
        JButton gradeBtn = new JButton("Assign Grade"); JButton remEnroll = new JButton("Remove"); JButton refresh = new JButton("Refresh");
        eBtns.add(gradeBtn); eBtns.add(remEnroll); eBtns.add(refresh);
        right.add(eBtns, BorderLayout.SOUTH);

        JPanel main = new JPanel(new GridLayout(1,3,8,8));
        main.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        main.add(left); main.add(mid); main.add(right);

        f.getContentPane().add(main, BorderLayout.CENTER);

        // --- actions (very small, using simple dialogs) ---
        addS.addActionListener(ae -> {
            String name = JOptionPane.showInputDialog(f,"Student name:");
            if (name==null || name.trim().isEmpty()) return;
            String email = JOptionPane.showInputDialog(f,"Email:");
            if (email==null || email.trim().isEmpty()) return;
            Student s = new Student(sid++, name.trim(), email.trim());
            students.add(s); studentModel.addElement(s);
        });

        delS.addActionListener(ae -> {
            Student sel = sList.getSelectedValue();
            if (sel==null) { JOptionPane.showMessageDialog(f,"Select a student."); return; }
            if (JOptionPane.showConfirmDialog(f,"Delete student and their enrollments?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
            // remove enrollments for student
            enrolls.removeIf(en -> {
                if (en.s.id==sel.id){ enrollModel.removeElement(en); return true; }
                return false;
            });
            students.remove(sel); studentModel.removeElement(sel);
        });

        viewT.addActionListener(ae -> {
            Student sel = sList.getSelectedValue();
            if (sel==null){ JOptionPane.showMessageDialog(f,"Select a student."); return; }
            StringBuilder sb = new StringBuilder();
            sb.append("Transcript for ").append(sel.name).append("\n\n");
            double pts=0; int cr=0;
            for (Enrollment en: enrolls){
                if (en.s.id!=sel.id) continue;
                sb.append(en.c.code).append(" | ").append(en.c.title).append(" | ").append(en.c.credits).append("cr | ");
                sb.append(en.g==null? "-" : en.g).append("\n");
                if (en.g!=null && isNumeric(en.g)){ pts += en.c.credits * points(en.g); cr += en.c.credits; }
            }
            sb.append("\n");
            if (cr>0) sb.append(String.format("GPA: %.2f", pts/cr)); else sb.append("GPA: N/A");
            JTextArea ta = new JTextArea(sb.toString()); ta.setEditable(false);
            JOptionPane.showMessageDialog(f,new JScrollPane(ta),"Transcript",JOptionPane.INFORMATION_MESSAGE);
        });

        addC.addActionListener(ae -> {
            String code = JOptionPane.showInputDialog(f,"Course code (CS101):");
            if (code==null || code.trim().isEmpty()) return;
            String title = JOptionPane.showInputDialog(f,"Title:");
            if (title==null || title.trim().isEmpty()) return;
            String crs = JOptionPane.showInputDialog(f,"Credits (int):");
            if (crs==null) return;
            int cr;
            try { cr = Integer.parseInt(crs.trim()); } catch(Exception ex){ JOptionPane.showMessageDialog(f,"Invalid credits"); return; }
            Course c = new Course(cid++, code.trim().toUpperCase(), title.trim(), cr);
            courses.add(c); courseModel.addElement(c);
        });

        delC.addActionListener(ae -> {
            Course sel = cList.getSelectedValue();
            if (sel==null){ JOptionPane.showMessageDialog(f,"Select a course."); return; }
            if (JOptionPane.showConfirmDialog(f,"Delete course and its enrollments?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
            enrolls.removeIf(en -> {
                if (en.c.id==sel.id){ enrollModel.removeElement(en); return true; }
                return false;
            });
            courses.remove(sel); courseModel.removeElement(sel);
        });

        enrollBtn.addActionListener(ae -> {
            Student s = sList.getSelectedValue(); Course c = cList.getSelectedValue();
            if (s==null || c==null){ JOptionPane.showMessageDialog(f,"Select student and course."); return; }
            boolean exists = enrolls.stream().anyMatch(en -> en.s.id==s.id && en.c.id==c.id);
            if (exists){ JOptionPane.showMessageDialog(f,"Already enrolled."); return; }
            Enrollment e = newEnrollment(s,c,null);
            enrolls.add(e);
        });

        gradeBtn.addActionListener(ae -> {
            Enrollment sel = eList.getSelectedValue();
            if (sel==null){ JOptionPane.showMessageDialog(f,"Select an enrollment."); return; }
            String choice = (String)JOptionPane.showInputDialog(f,"Pick grade:", "Grade", JOptionPane.PLAIN_MESSAGE, null, Arrays.stream(Grade.values()).map(Enum::name).toArray(), sel.g==null? "A" : sel.g.name());
            if (choice==null) return;
            try { sel.g = Grade.valueOf(choice); eList.repaint(); } catch(Exception ex){ JOptionPane.showMessageDialog(f,"Invalid grade"); }
        });

        remEnroll.addActionListener(ae -> {
            Enrollment sel = eList.getSelectedValue();
            if (sel==null){ JOptionPane.showMessageDialog(f,"Select an enrollment."); return; }
            if (JOptionPane.showConfirmDialog(f,"Remove this enrollment?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
            enrolls.remove(sel); enrollModel.removeElement(sel);
        });

        refresh.addActionListener(ae -> { sList.repaint(); cList.repaint(); eList.repaint(); });

        f.setVisible(true);
    }

    private static boolean isNumeric(Grade g){ return g==Grade.A||g==Grade.B||g==Grade.C||g==Grade.D||g==Grade.F; }
    private static int points(Grade g){ switch(g){case A:return 10;case B:return 8;case C:return 6;case D:return 4;case F:return 0;default:return 0;}}

    /* --- Main --- */
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            ELearningPlatform app = new ELearningPlatform();
            app.seed();
            app.buildAndShowGui();
        });
    }
}
