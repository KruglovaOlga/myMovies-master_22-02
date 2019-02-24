/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.QueryTimeoutException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import services.ConnectionManager;

/**
 *
 * @author User
 */
public class Search_Form extends javax.swing.JFrame {
    private EntityManager em;
    private List rst;               // λίστα αποτελεσμάτων μίας εγγραφής από query
    private List<Object[]> rstAll;  // λίστα πολλαπλών αποτελεσμάτων από query
    private Query query;            // για καταχώρηση query από entity ή απλό sql
    private String[] idGenre;       // πίνακας με τα ids των Ειδών Ταινιών
    private int intGenre;           // δείκτης του πίνακα idGenre
    private String idCurrentGenre;  // το τρέχον id του Είδους Ταινίας
    private String[] idFl;          // πίνακας με τα ids της Λίστας Αγαπημένων
    private int intFl;              // δείκτης του πίνακα idFl
    private String idCurrentFl;     // το τρέχον id της Λίστας Αγαπημένων
    
    private DefaultTableModel tableModel;   // default jtable model
    
    private final String msgDialogTitle = "Αναζήτηση Ταινιών";
    private final Object nll = "null";
    private int updateFl = 0;
    
    /**
     * Creates new form Search_Form
     */
    public Search_Form() {
        initComponents();
        setLocationRelativeTo(null);
        tableModel = (DefaultTableModel) tblMovie.getModel();
    
        fill_cmbGenre();
    }

    private void fill_cmbGenre() {
        try {
            // query από entity class Genre
            query = em.createNamedQuery("Genre.findAll", model.Genre.class);
            //Αποτελέσματα αναζήτησης σε λίστα
            genreList = query.getResultList();
        } 
        catch (Exception e) { //αν δεν βρει δεδομένα να εμφανίσει μήνυμα
            JOptionPane.showMessageDialog(null, 
                    "Δεν υπάρχουν Είδη Ταινιων", 
                    msgDialogTitle,
                    JOptionPane.ERROR_MESSAGE);
        }
        
        // προσδιορισμός μεγέθους πίνακα ίσο με αριθμό αποτελεσμάτων του query
        idGenre = new String[genreList.size() + 1];
        intGenre = 0;
        
        //loop για κάθε αποτέλεσμα της αναζήτησης του query
        for (model.Genre genre : genreList) {
            //καταχωρεί στον πίνακα το id του Είδους της Ταινίας
            idGenre[intGenre] = genre.getId().toString();
            String comboName = genre.getName() + " (" + idGenre[intGenre] + ")";
            intGenre++;
            // προσθέτει το Είδος Ταινίας στο combo box
            cmbGenre.addItem(comboName);
        }
        // εξ ορισμού επιλεγμένη η θέση 0 στο combo box Είδος Ταινίας
        cmbGenre.setSelectedIndex(0);
    }
    
    private void fill_cmbFavoriteList(int inFlId) {
        // καθάρισε το περιεχόμενο του combo box 'Προσθήκη σε Λίστα'
        updateFl = 0;
        cmbFavoriteList.removeAllItems();
        
        // αν η Ταινία ανήκει σε κάποια Λίστα Αγαπημένων το combo box
        // θα προβάλη την Αγαπημένη Λίστα
        int startAtRow = 0; // η γραμμή του combo box που ο κωδικός = inFlId
        
        try {
            // query από entity class FavoriteList
            query = em.createNamedQuery("FavoriteList.findAll", model.FavoriteList.class);
            //Αποτελέσματα αναζήτησης σε λίστα
            flList = query.getResultList();
        } 
        catch (Exception e) { //αν δεν βρει δεδομένα να εμφανίσει μήνυμα
            JOptionPane.showMessageDialog(null, 
                    "Δεν υπάρχουν Λίστα Αγαπημένων", 
                    msgDialogTitle,
                    JOptionPane.ERROR_MESSAGE);
        }
        
        // προσδιορισμός μεγέθους πίνακα ίσο με αριθμό αποτελεσμάτων του query
        idFl = new String[flList.size() + 1];
        intFl = 0;
        
        // ...
        System.out.println("inFlId >" + inFlId + "<");
        if (inFlId == 0) {
            idFl[intFl] = "0";
            String comboName = "Δεν ανήκει σε Λίστα Αγαπημένων (0)";
            cmbFavoriteList.addItem(comboName);
            intFl++;
        }
        
        //loop για κάθε αποτέλεσμα της αναζήτησης του query
        for (model.FavoriteList favoriteList : flList) {
            //καταχωρεί στον πίνακα το id της Λίστας Αγαπημένων
            idFl[intFl] = favoriteList.getId().toString();
            String comboName = favoriteList.getName() + " (" + idFl[intFl] + ")";
            
            if ( favoriteList.getId() == inFlId) {
                startAtRow = intFl;
            }
            
            intFl++;
            //προσθέτει την Λίστα Αγαπημένων στο combo box
            cmbFavoriteList.addItem(comboName);
        }
        // Αν η ταινία ανήκει σε κάποια Λίστα Αγαπημένων εμφανίζει
        // την τιμή αυτή αλλιώς ...
        cmbFavoriteList.setSelectedIndex(startAtRow);
    }
    
    private void fillTable(String sql) {
        Search_Form sf = new Search_Form(); // default constructor
        EntityManager em = new ConnectionManager().getEm(); // Entity Manager
        
        // Δημιουργία query
        Query  q = em.createNativeQuery(sql);
        try {
            //Αποτελέσματα αναζήτησης σε λίστα
            rstAll = q.getResultList();
            
            if (rstAll.isEmpty()) {
                JOptionPane.showMessageDialog(null, 
                        "Δεν βρέθηκαν ταινίες", 
                        msgDialogTitle,
                        JOptionPane.WARNING_MESSAGE);
                
                return;
            }
            
            for (Object[] a : rstAll) {
                tableModel.insertRow(tblMovie.getRowCount(),
                        new Object[]{a[0], a[1], a[2], a[3], a[4]});
                System.out.println(">" + a[4] + "<");
            }
            em.close();
        }
        catch (QueryTimeoutException e) { //αν λήξει ο χρόνος αναμονής αποτελεσμ.
            System.out.println(e.getLocalizedMessage());
        }
        catch (Exception e) {
            System.out.println("PROBLEM in query search Movies");
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        myMoviesPUEntityManager = java.beans.Beans.isDesignTime() ? null : javax.persistence.Persistence.createEntityManagerFactory("myMoviesPU").createEntityManager();
        genreQuery = java.beans.Beans.isDesignTime() ? null : myMoviesPUEntityManager.createQuery("SELECT g FROM Genre g");
        genreList = java.beans.Beans.isDesignTime() ? java.util.Collections.emptyList() : genreQuery.getResultList();
        movieQuery = java.beans.Beans.isDesignTime() ? null : myMoviesPUEntityManager.createQuery("SELECT m FROM Movie m");
        movieList = java.beans.Beans.isDesignTime() ? java.util.Collections.emptyList() : movieQuery.getResultList();
        tfReleaseYear = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblMovie = new javax.swing.JTable();
        lblGenre = new javax.swing.JLabel();
        lblReleaseYear = new javax.swing.JLabel();
        lblSearchMovie_Title = new javax.swing.JLabel();
        cmbGenre = new javax.swing.JComboBox<>();
        cmbClearFavoriteList = new javax.swing.JButton();
        btnClearCriteria = new javax.swing.JButton();
        cmbFavoriteList = new javax.swing.JComboBox<>();
        lblFavoriteList = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tfReleaseYear.setToolTipText("");

        btnSearch.setText("Αναζήτηση");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        tblMovie.setAutoCreateRowSorter(true);
        tblMovie.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Τιτλος Ταινίας", "Βαθμολογία", "Περιγραφή", "Κωδικός Ταινίας", "Κωδικός Λίστας"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Double.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblMovie.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblMovieMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblMovie);
        if (tblMovie.getColumnModel().getColumnCount() > 0) {
            tblMovie.getColumnModel().getColumn(3).setMinWidth(0);
            tblMovie.getColumnModel().getColumn(3).setPreferredWidth(0);
            tblMovie.getColumnModel().getColumn(3).setMaxWidth(0);
            tblMovie.getColumnModel().getColumn(4).setMinWidth(0);
            tblMovie.getColumnModel().getColumn(4).setPreferredWidth(0);
            tblMovie.getColumnModel().getColumn(4).setMaxWidth(0);
        }

        lblGenre.setText("Είδος Ταινίας");

        lblReleaseYear.setText("Έτος Κυκλοφορίας");

        lblSearchMovie_Title.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblSearchMovie_Title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSearchMovie_Title.setText("Αναζήτηση Ταινιών");

        cmbGenre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbGenreActionPerformed(evt);
            }
        });

        cmbClearFavoriteList.setText("Αφαίρεση από Λίστα");
        cmbClearFavoriteList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbClearFavoriteListActionPerformed(evt);
            }
        });

        btnClearCriteria.setText("Καθαρισμός Κριτηρίων");
        btnClearCriteria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearCriteriaActionPerformed(evt);
            }
        });

        cmbFavoriteList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbFavoriteListItemStateChanged(evt);
            }
        });
        cmbFavoriteList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFavoriteListActionPerformed(evt);
            }
        });

        lblFavoriteList.setText("Προσθήκη σε Λίστα");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblFavoriteList, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbFavoriteList, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                        .addComponent(cmbClearFavoriteList)
                        .addGap(72, 72, 72))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(btnSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClearCriteria)
                        .addGap(136, 136, 136))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(lblSearchMovie_Title, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(lblReleaseYear, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                                        .addComponent(lblGenre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(tfReleaseYear, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cmbGenre, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(69, 69, 69)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSearchMovie_Title, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGenre, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbGenre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tfReleaseYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(lblReleaseYear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSearch)
                    .addComponent(btnClearCriteria))
                .addGap(26, 26, 26)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbClearFavoriteList)
                    .addComponent(cmbFavoriteList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFavoriteList, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // καθάρισε το περιεχόμενο του combo box 'Προσθήκη σε Λίστα'
        updateFl = 0;
        cmbFavoriteList.removeAllItems();
        // έτος κυκλοφορίας που πληκτρολόγησε ο χρήστης
        String ry = new String(this.tfReleaseYear.getText()); 
               
        //έλεγχος ότι το Έτος Κυκλοφορίας έχει τιμή
        if (ry.equals("")) {
        // if (ry != 0 )
            JOptionPane.showMessageDialog(null, 
                    "Το Έτος Κυκλοφορίας δεν έχει τιμή", 
                    msgDialogTitle,
                    JOptionPane.ERROR_MESSAGE);
            
            return;
        }
                
        //αν ο πίνακας έχει δεδομένα διέγραψε τα
        if (tblMovie.getRowCount() > 0) {
            while (tblMovie.getRowCount() > 0) {
                tableModel.removeRow(tableModel.getRowCount() - 1);
            }
        }
                
        // Αναζήτηση ταινιών βάση των κριτηρίων
        // Είδος Ταινίας και Έτος Κυκλοφορίας
        String sql = "SELECT m.title, "
                + "m.rating, "
                + "m.overview, "
                + "m.id, "
                + "m.favorite_list_Id "
                + "FROM Movie m "
                + "WHERE YEAR(m.release_Date) = " + ry + " "
                + "AND m.genre_Id = " + idCurrentGenre + " "
                + "ORDER BY m.rating DESC"
                ;
        System.out.println(sql);
        fillTable(sql);
                
    }//GEN-LAST:event_btnSearchActionPerformed

    private void cmbGenreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbGenreActionPerformed
        idCurrentGenre = idGenre[cmbGenre.getSelectedIndex()];
    }//GEN-LAST:event_cmbGenreActionPerformed

    private void cmbClearFavoriteListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbClearFavoriteListActionPerformed
        // έλεγχος ότι ο χρήστης έχει επιλέξει ταινία
        if (tblMovie.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(null, 
                    "Δεν έχει επιλεχθεί ταινία", 
                    msgDialogTitle,
                    JOptionPane.WARNING_MESSAGE);
                
            return;
        }

        // λαμβάνω από το jTable τον Κωδικό της Ταινίας
        int row = tblMovie.getSelectedRow();
        String mId = tblMovie.getModel().getValueAt(row, 3).toString();
        System.out.println(mId);
        idCurrentFl = idFl[cmbFavoriteList.getSelectedIndex()];
        
        if (idCurrentFl.equals("0")) {
            JOptionPane.showMessageDialog(null, 
                    "Η Ταινία δεν ανήκει σε κάποια Λίστα Αγαπημένων", 
                    msgDialogTitle,
                    JOptionPane.WARNING_MESSAGE);
                
            return;
        }
        
        String sql_unull = "UPDATE Movie m "
                + "SET m.FAVORITE_LIST_ID = NULL "
                + "WHERE m.id = " + mId
                ;
        System.out.println(sql_unull);
        
        try {
            EntityManager em = new ConnectionManager().getEm();
            em.getTransaction().begin();
            Query  q = em.createNativeQuery(sql_unull);
            q.executeUpdate();          
            em.getTransaction().commit();
            em.close();
        }
        catch (Exception e) {
            System.out.println("PROBLEM in update favorite_list_id to null");
        }
        
        // καθάρισε το περιεχόμενο του combo box 'Προσθήκη σε Λίστα'
        updateFl = 0;
        cmbFavoriteList.removeAllItems();
    }//GEN-LAST:event_cmbClearFavoriteListActionPerformed

    private void tblMovieMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblMovieMouseClicked
        // ...
        updateFl = 0;
        // λαμβάνω από το jTable τον Κωδικό της Λίστας Αγαπημένων
        int row = tblMovie.getSelectedRow();
        System.out.println("event >" + tblMovie.getModel().getValueAt(row, 4) + "<");
        String s = String.valueOf(tblMovie.getModel().getValueAt(row, 4));
        
        if (s == nll) {    // η ταινία δεν ανήκει σε κάποια Λίστα Αγαπημένων
            System.out.println("Η ταινία δεν ανήκει σε κάποια Λίστα Αγαπημένων");
            
            fill_cmbFavoriteList(0);
        }
        else {
            System.out.println("is NOT empty`");
            
            String strFlId = tblMovie.getModel().getValueAt(row, 4).toString();
            int flId = Integer.parseInt(strFlId);
            System.out.println(">" + flId + "<");
                  
            fill_cmbFavoriteList(flId);
        }
        
        updateFl = 1;
    }//GEN-LAST:event_tblMovieMouseClicked

    private void cmbFavoriteListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFavoriteListActionPerformed
        System.out.println("cmbFavoriteListActionPerformed !!");
        if (updateFl == 0) {
            System.out.println("DON'T Proceed");
            
            return;
        }
        else {
            System.out.println("Proceed !!");
            
            // λαμβάνω από το jTable τον προηγούμενο Κωδικό της Λίστα Αγαπημένων
            int row = tblMovie.getSelectedRow();
            String pFlId = String.valueOf(tblMovie.getModel().getValueAt(row, 4));
            // λαμβάνω από το combo box τον νέο Κωδικό της Λίστα Αγαπημένων
            idCurrentFl = idFl[cmbFavoriteList.getSelectedIndex()];
        
            System.out.println("p.FlId: " + pFlId + " n.FlId: " + idCurrentFl);
        
            // μη αποδεκτή τιμή
            if (idCurrentFl.equals("0")) {
                JOptionPane.showMessageDialog(null, 
                        "Μη αποδεκτή τιμή", 
                        msgDialogTitle,
                        JOptionPane.WARNING_MESSAGE);
                
                return;
            }
            
            // δεν έχει αλλάξει η Λίστα Αγαπημένων
            if (pFlId.equals(idCurrentFl)) {
                return;
            }
            
            // προχωράω με την ενημέρωση της Λίστας Αγαπημένων
            
            // λαμβάνω από το jTable τον Κωδικό της Ταινίας
            // int row = tblMovie.getSelectedRow();
            String mId = tblMovie.getModel().getValueAt(row, 3).toString();
            
            String sql_unew = "UPDATE Movie m "
                    + "SET m.FAVORITE_LIST_ID = " + idCurrentFl + " "
                    + "WHERE m.id = " + mId
                    ;
            System.out.println(sql_unew);
        
            try {
                EntityManager em = new ConnectionManager().getEm();
                em.getTransaction().begin();
                Query  q = em.createNativeQuery(sql_unew);
                q.executeUpdate();          
                em.getTransaction().commit();
                em.close();
            }
            catch (Exception e) {
                System.out.println("PROBLEM in update favorite_list_id to new value");
            }
        }
    }//GEN-LAST:event_cmbFavoriteListActionPerformed

    private void cmbFavoriteListItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbFavoriteListItemStateChanged
        // System.out.println("*** cmbFavoriteListItemStateChanged");
    }//GEN-LAST:event_cmbFavoriteListItemStateChanged

    private void btnClearCriteriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearCriteriaActionPerformed
        // τοποθέτησε στην πρώτη επιλογή το combo box Είδος Ταινίας
        cmbGenre.setSelectedIndex(0);
        // καθαρισμός Έτους Κυκλοφορίας
        this.tfReleaseYear.setText("");
        //αν το jTable έχει δεδομένα διέγραψε τα
        if (tblMovie.getRowCount() > 0) {
            while (tblMovie.getRowCount() > 0) {
                tableModel.removeRow(tableModel.getRowCount() - 1);
            }
        }
        // καθάρισε το περιεχόμενο του combo box 'Προσθήκη σε Λίστα'
        updateFl = 0;
        cmbFavoriteList.removeAllItems();
        
    }//GEN-LAST:event_btnClearCriteriaActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        // default constructor
        Search_Form sf = new Search_Form();
        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Search_Form.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Search_Form.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Search_Form.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Search_Form.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Search_Form().setVisible(true);
            }
        });
    }

    // my Variables declaration 
    private java.util.List<model.FavoriteList> flList;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClearCriteria;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton cmbClearFavoriteList;
    private javax.swing.JComboBox<String> cmbFavoriteList;
    private javax.swing.JComboBox<String> cmbGenre;
    private java.util.List<model.Genre> genreList;
    private javax.persistence.Query genreQuery;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblFavoriteList;
    private javax.swing.JLabel lblGenre;
    private javax.swing.JLabel lblReleaseYear;
    private javax.swing.JLabel lblSearchMovie_Title;
    private java.util.List<model.Movie> movieList;
    private javax.persistence.Query movieQuery;
    private javax.persistence.EntityManager myMoviesPUEntityManager;
    private javax.swing.JTable tblMovie;
    private javax.swing.JTextField tfReleaseYear;
    // End of variables declaration//GEN-END:variables

}
