package transportecostominimo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Ventanaprincipal extends JFrame {

    private JTextField txtFilas, txtColumnas;
    private JButton btnGenerar, btnEjemplo, btnResolver, btnLimpiar;
    private JTable tabla, tablaAsignacion;
    private JTextArea areaResultado, areaContexto;

    public Ventanaprincipal() {
        setTitle("Asignación de Tickets de Soporte - Método Costo Mínimo");
        setSize(1150, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Panel título ---
        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setBackground(Color.decode("#004d99"));
        JLabel lblTitulo = new JLabel("Asignación de Tickets de Soporte");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelTitulo.add(lblTitulo, BorderLayout.CENTER);

        // --- Panel controles ---
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTop.setBackground(Color.decode("#e6f7ff"));

        panelTop.add(new JLabel("Orígenes (Técnicos):"));
        txtFilas = new JTextField("3", 4);
        panelTop.add(txtFilas);

        panelTop.add(new JLabel("Destinos (Zonas):"));
        txtColumnas = new JTextField("3", 4);
        panelTop.add(txtColumnas);

        btnGenerar = new JButton("➕ Generar matriz");
        btnEjemplo  = new JButton("📄 Ejemplo");
        btnResolver = new JButton("⚙ Resolver");
        btnLimpiar  = new JButton("🧹 Limpiar");

        // Colores de botones
        btnGenerar.setBackground(Color.decode("#66b3ff"));
        btnEjemplo.setBackground(Color.decode("#99ccff"));
        btnResolver.setBackground(Color.decode("#5cd65c"));
        btnLimpiar.setBackground(Color.decode("#ff6666"));

        panelTop.add(btnGenerar);
        panelTop.add(btnEjemplo);
        panelTop.add(btnResolver);
        panelTop.add(btnLimpiar);

        // --- Panel contexto ---
        String contexto = "📌 Contexto del problema:\n"
                + "- Técnicos (orígenes): Carlos, Ana, Javier.\n"
                + "- Zonas (destinos): Norte, Centro, Sur.\n"
                + "- Oferta: tickets que cada técnico puede atender.\n"
                + "- Demanda: tickets pendientes por zona.\n"
                + "- Costos: tiempo (minutos) por ticket según técnico y zona.\n\n"
                + "🎯 Objetivo: Minimizar el tiempo total de atención asignando tickets óptimamente.";
        areaContexto = new JTextArea(contexto);
        areaContexto.setEditable(false);
        areaContexto.setLineWrap(true);
        areaContexto.setWrapStyleWord(true);
        areaContexto.setBackground(Color.decode("#ffffe6"));
        JScrollPane scrollContexto = new JScrollPane(areaContexto);
        scrollContexto.setPreferredSize(new Dimension(320, 200));

        // --- Tablas ---
        tabla = new JTable();
        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Matriz de entrada (costos, oferta, demanda)"));

        tablaAsignacion = new JTable();
        JScrollPane scrollAsignacion = new JScrollPane(tablaAsignacion);
        scrollAsignacion.setBorder(BorderFactory.createTitledBorder("Matriz de asignaciones (resultado)"));

        // Divisor vertical
        JSplitPane splitTablas = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTabla, scrollAsignacion);
        splitTablas.setResizeWeight(0.5);

        // --- Área de resultados ---
        areaResultado = new JTextArea(10, 50);
        areaResultado.setEditable(false);
        areaResultado.setBackground(Color.decode("#f0fff0"));
        JScrollPane scrollResultado = new JScrollPane(areaResultado);
        scrollResultado.setBorder(BorderFactory.createTitledBorder("Explicación y costo"));

        // --- Layout ---
        JPanel panelCentro = new JPanel(new BorderLayout(8,8));
        panelCentro.add(splitTablas, BorderLayout.CENTER);

        setLayout(new BorderLayout(8,8));
        add(panelTitulo, BorderLayout.NORTH);
        add(panelTop, BorderLayout.BEFORE_FIRST_LINE);
        add(panelCentro, BorderLayout.CENTER);
        add(scrollContexto, BorderLayout.EAST);
        add(scrollResultado, BorderLayout.SOUTH);

        // --- Eventos ---
        btnGenerar.addActionListener(e -> generarMatriz());
        btnEjemplo.addActionListener(e -> cargarEjemplo());
        btnResolver.addActionListener(e -> resolverModelo());
        btnLimpiar.addActionListener(e -> limpiar());

        generarMatriz();
    }

    private void generarMatriz() {
        int m = parseIntSafe(txtFilas.getText(), 1);
        int n = parseIntSafe(txtColumnas.getText(), 1);

        String[] columnas = new String[n+2];
        columnas[0] = "Técnico/Zona";
        for (int j=1;j<=n;j++) columnas[j] = "Zona "+j;
        columnas[n+1] = "Oferta";

        DefaultTableModel model = new DefaultTableModel(columnas, m+1);
        for (int i=0;i<m;i++) model.setValueAt("Técnico "+(i+1), i, 0);
        model.setValueAt("Demanda", m, 0);

        tabla.setModel(model);
        tablaAsignacion.setModel(new DefaultTableModel()); // limpiar
        areaResultado.setText("✅ Matriz generada.\n");
    }

    private void cargarEjemplo() {
        txtFilas.setText("3");
        txtColumnas.setText("3");
        generarMatriz();
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        double[][] costos = { {12,8,15}, {20,10,12}, {25,18,10} };
        double[] oferta = {6,7,7};
        double[] demanda = {5,8,7};

        for (int i=0;i<3;i++) {
            for (int j=1;j<=3;j++) model.setValueAt(costos[i][j-1], i, j);
            model.setValueAt(oferta[i], i, 4);
        }
        for (int j=1;j<=3;j++) model.setValueAt(demanda[j-1], 3, j);

        areaResultado.setText("📄 Ejemplo cargado.\n");
    }

    private void resolverModelo() {
        try {
            DefaultTableModel model = (DefaultTableModel) tabla.getModel();
            int m = parseIntSafe(txtFilas.getText(),1);
            int n = parseIntSafe(txtColumnas.getText(),1);

            double[][] costos = new double[m][n];
            double[] oferta = new double[m];
            double[] demanda = new double[n];

            for (int i=0;i<m;i++) {
                for (int j=0;j<n;j++) costos[i][j] = toDouble(model.getValueAt(i, j+1));
                oferta[i] = toDouble(model.getValueAt(i, n+1));
            }
            for (int j=0;j<n;j++) demanda[j] = toDouble(model.getValueAt(m, j+1));

            Modelotransporte mt = new Modelotransporte(costos, oferta, demanda);
            Modelotransporte.Resultado res = mt.resolverConPasos();

            // --- Matriz de asignaciones ---
            String[] cols = new String[res.colLabels.length+1];
            cols[0] = "Técnico/Zona";
            for (int j=1;j<cols.length;j++) cols[j] = res.colLabels[j-1];

            DefaultTableModel out = new DefaultTableModel(cols, res.rowLabels.length);
            for (int i=0;i<res.rowLabels.length;i++) out.setValueAt(res.rowLabels[i], i, 0);

            for (int i=0;i<res.rowLabels.length;i++) {
                for (int j=0;j<res.colLabels.length;j++) {
                    double x = res.asignacion[i][j];
                    out.setValueAt(x>0? String.format("%.0f (c=%.2f)",x,costos[i][j]):"-", i, j+1);
                }
            }
            tablaAsignacion.setModel(out);

            // --- Explicación ---
            StringBuilder sb = new StringBuilder("🔎 Explicación paso a paso:\n\n");
            int k=1;
            for (Modelotransporte.Paso p : res.pasos) {
                sb.append(String.format("Paso %d: Se elige (%s → %s) por costo mínimo c=%.2f. ",
                        k++, res.rowLabels[p.i], res.colLabels[p.j], p.c));
                sb.append(String.format("Se asignan %.0f tickets.\n", p.qty));
            }
            sb.append("\n📦 Asignaciones finales:\n");
            for (int i=0;i<res.rowLabels.length;i++) {
                for (int j=0;j<res.colLabels.length;j++) {
                    if (res.asignacion[i][j]>0)
                        sb.append(String.format("%s → %s : %.0f tickets (c=%.2f)\n",
                                res.rowLabels[i], res.colLabels[j], res.asignacion[i][j], costos[i][j]));
                }
            }

            sb.append(String.format("\n💡 Costo total mínimo = %.2f minutos\n", res.costoTotal));
            areaResultado.setText(sb.toString());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: "+e.getMessage());
        }
    }

    private void limpiar() {
        tabla.setModel(new DefaultTableModel());
        tablaAsignacion.setModel(new DefaultTableModel());
        areaResultado.setText("");
    }

    private int parseIntSafe(String s,int d){ try{return Integer.parseInt(s.trim());}catch(Exception e){return d;}}
    private double toDouble(Object v){ try{return Double.parseDouble(v.toString());}catch(Exception e){return 0;}}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Ventanaprincipal().setVisible(true));
    }
}
