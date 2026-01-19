package transportecostominimo;
import java.util.ArrayList;
import java.util.List;

public class Modelotransporte {

    public static class Paso {
        public final int i, j;
        public final double c;
        public final double qty;
        public final double[] S;
        public final double[] D;
        public Paso(int i, int j, double c, double qty, double[] S, double[] D){
            this.i=i; this.j=j; this.c=c; this.qty=qty; this.S=S; this.D=D;
        }
    }

    public static class Resultado {
        public final double[][] asignacion;
        public final double costoTotal;
        public final List<Paso> pasos;
        public final String[] rowLabels;
        public final String[] colLabels;
        public Resultado(double[][] asignacion, double costoTotal, List<Paso> pasos,
                         String[] rowLabels, String[] colLabels){
            this.asignacion = asignacion;
            this.costoTotal = costoTotal;
            this.pasos = pasos;
            this.rowLabels = rowLabels;
            this.colLabels = colLabels;
        }
    }

    private double[][] costos;
    private double[] oferta;
    private double[] demanda;

    public Modelotransporte(double[][] costos, double[] oferta, double[] demanda) {
        this.costos = costos;
        this.oferta = oferta;
        this.demanda = demanda;
    }

    // --- Balanceo automático con fila/columna dummy ---
    private void balancear() {
        double sumO = 0, sumD = 0;
        for (double v: oferta) sumO += v;
        for (double v: demanda) sumD += v;

        if (Math.abs(sumO - sumD) < 1e-9) return; // ya está balanceado

        if (sumO > sumD) {
            // agregar columna dummy
            double diff = sumO - sumD;
            int m = oferta.length, n = demanda.length;
            double[][] newC = new double[m][n+1];
            for (int i=0;i<m;i++){
                System.arraycopy(costos[i], 0, newC[i], 0, n);
                newC[i][n] = 0; // costos dummy = 0
            }
            costos = newC;

            double[] newD = new double[n+1];
            System.arraycopy(demanda, 0, newD, 0, n);
            newD[n] = diff;
            demanda = newD;
        } else {
            // agregar fila dummy
            double diff = sumD - sumO;
            int m = oferta.length, n = demanda.length;
            double[][] newC = new double[m+1][n];
            for (int i=0;i<m;i++) System.arraycopy(costos[i], 0, newC[i], 0, n);
            for (int j=0;j<n;j++) newC[m][j] = 0; // fila dummy

            costos = newC;

            double[] newO = new double[m+1];
            System.arraycopy(oferta, 0, newO, 0, m);
            newO[m] = diff;
            oferta = newO;
        }
    }

    // --- Resolver con pasos ---
    public Resultado resolverConPasos() {
        balancear();

        int m = oferta.length, n = demanda.length;
        double[][] X = new double[m][n];
        double[] S = oferta.clone();
        double[] D = demanda.clone();
        List<Paso> pasos = new ArrayList<>();

        while (true) {
            int bestI=-1, bestJ=-1;
            double bestC=Double.MAX_VALUE;
            for (int i=0;i<m;i++) if (S[i]>0) {
                for (int j=0;j<n;j++) if (D[j]>0) {
                    if (costos[i][j] < bestC) {
                        bestC = costos[i][j];
                        bestI = i;
                        bestJ = j;
                    }
                }
            }
            if (bestI==-1) break;

            double qty = Math.min(S[bestI], D[bestJ]);
            X[bestI][bestJ]+=qty;
            S[bestI]-=qty;
            D[bestJ]-=qty;

            pasos.add(new Paso(bestI, bestJ, bestC, qty, S.clone(), D.clone()));
        }

        double costoTotal=0;
        for (int i=0;i<m;i++) for (int j=0;j<n;j++) costoTotal += X[i][j]*costos[i][j];

        // etiquetas
        String[] rowLabels = new String[m];
        for (int i=0;i<m;i++) rowLabels[i] = (i<m-1? "Técnico "+(i+1): "Dummy");
        String[] colLabels = new String[n];
        for (int j=0;j<n;j++) colLabels[j] = (j<n-1? "Zona "+(j+1): "Dummy");

        return new Resultado(X,costoTotal,pasos,rowLabels,colLabels);
    }
}
