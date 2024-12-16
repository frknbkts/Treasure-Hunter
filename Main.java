import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class Konum {
    public int x;
    public int y;

    public Konum(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Konum konum = (Konum) obj;
        return x == konum.x && y == konum.y;
    }
}

abstract class Engel {
    protected Konum konum;

    public Engel(Konum konum) {
        this.konum = konum;
    }

    public Konum getKonum() {
        return konum;
    }


    public abstract boolean gecilebilirMi();
}

class DinamikEngel extends Engel {
    private int hareketAraligi;
    private char hareketEkseni;
    private int sonHareketYonu = -1;
    public DinamikEngel(Konum konum, int hareketAraligi, char hareketEkseni) {
        super(konum);
        this.hareketAraligi = hareketAraligi;
        this.hareketEkseni = hareketEkseni;
    }

    @Override
    public boolean gecilebilirMi() {
        return false;
    }

    public void hareketEt() {
        int hareketYonu;

        do {
            hareketYonu = (int) (Math.random() * 2);
        } while (hareketYonu == sonHareketYonu);

        char orijinalHücreDeğeri = Main.grid[konum.getX()][konum.getY()];

        Main.grid[konum.getX()][konum.getY()] = '0';

        switch (hareketYonu) {
            case 0: // Eksen x ise sola, eksen y ise aşağıya hareket et
                if (hareketEkseni == 'x' && konum.getY() - 1 >= 0) {
                    konum = new Konum(konum.getX(), konum.getY() - 1);
                    Main.grid[konum.getX()][konum.getY() + 1] = '0';
                } else if (hareketEkseni == 'y') {
                    konum = new Konum(konum.getX() + 1, konum.getY());
                    Main.grid[konum.getX() - 1][konum.getY()] = '0';
                } else {
                    konum = new Konum(konum.getX() + 1, konum.getY());
                    Main.grid[konum.getX() - 1][konum.getY()] = '0';
                }
                break;
            case 1: // Eksen x ise sağa, eksen y ise yukarıya hareket et
                if (hareketEkseni == 'x' && konum.getY() + 1 < Main.sutunlar) {
                    konum = new Konum(konum.getX(), konum.getY() + 1);
                    Main.grid[konum.getX()][konum.getY() - 1] = '0';
                } else if (hareketEkseni == 'y') {
                    konum = new Konum(konum.getX() - 1, konum.getY());
                    Main.grid[konum.getX() + 1][konum.getY()] = '0';
                } else {
                    konum = new Konum(konum.getX() - 1, konum.getY());
                    Main.grid[konum.getX() + 1][konum.getY()] = '0';
                }
                break;
            default:
                break;
        }

        Main.grid[konum.getX()][konum.getY()] = 'M';

        sonHareketYonu = hareketYonu;
    }
}

public class Main extends JFrame {
    static ArrayList<Integer> hazineBulundu = new ArrayList<>();
    private static final int HÜCRE_BOYUTU = 30;
    private static final Color MAVI_RENK = new Color(0, 0, 255);
    private static final Color SARI_RENK = new Color(255, 255, 0);
    private static final Color YEŞIL_RENK = new Color(0, 255, 0);
    private static final Color TURUNCU_RENK = new Color(255, 165, 0);
    static char[][] grid;
    static int satirlar;
    static int sutunlar;
    private static ArrayList<Konum> gKonumlari = new ArrayList<>();
    private static ArrayList<Konum> sKonumlari = new ArrayList<>();
    private static ArrayList<Konum> yKonumlari = new ArrayList<>();
    private static ArrayList<Konum> mKonumlari = new ArrayList<>();
    private static ArrayList<Konum> DKonumlari = new ArrayList<>();
    private static Konum avcıPozisyonu;
    private static ArrayList<Konum> keşfedilenHazine = new ArrayList<>();
    private static ArrayList<char[][]> durumlar = new ArrayList<>();
    public static KontrolPenceresi kontrolPenceresi = new KontrolPenceresi();
    private static int mevcutDurum = 0;
    private JLabel[][] etiketler;
    private int hazineTakibi;
    public static int k=0;
    private ArrayList<DinamikEngel> dinamikEngeller = new ArrayList<>();

    private int toplananHazineSayisi= 0;

    public Main() {
        // Buton işlemine taşındı
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int tusKodu = e.getKeyCode();
                if (tusKodu == KeyEvent.VK_RIGHT && mevcutDurum < durumlar.size() - 1) {
                    mevcutDurum++;
                    etiketleriGüncelle();
                } else if (tusKodu== KeyEvent.VK_LEFT && mevcutDurum < durumlar.size()-1) {
                    mevcutDurum--;
                    etiketleriGüncelle();
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::olusturVeGöster);
    }

    private static void olusturVeGöster() {
        kontrolPenceresi.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        kontrolPenceresi.setVisible(true);
    }

    void anaPencereyiBaşlat() {
        setTitle("Yazlab 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dosyadanGridOku();
        dinamikEngelEkle();
        etiketleriBaşlat();
        dfs(new Konum(0, 0));
        durumlar.add(gridKopyasınıAl(grid));

        etiketleriGüncelle();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setFocusable(true);
    }

    private void dfs(Konum başlangıç) {
        hazineTakibi++;
        boolean[][] ziyaretEdildi = new boolean[satirlar][sutunlar];
        Stack<Konum> yığın = new Stack<>();
        yığın.push(başlangıç);

        while (!yığın.isEmpty()) {
            Konum güncel = yığın.pop();
            int x = güncel.x;
            int y = güncel.y;

            if (!ziyaretEdildi[x][y]) {
                ziyaretEdildi[x][y] = true;
                if (grid[x][y] == 'G' || grid[x][y] == 'A') {
                    keşfedilenHazine.add(new Konum(x, y));
                    System.out.println("Hazine:" + güncel.x + "," + güncel.y);
                    hazineBulundu.add(hazineTakibi);
                    grid[x][y]='F';
                }
                else {
                    grid[x][y] = '-';
                }
                int[][] yönler = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                for (int[] yön : yönler) {
                    int yeniX = x + yön[0];
                    int yeniY = y + yön[1];

                    if (geçerliMi(yeniX, yeniY) && grid[yeniX][yeniY] != 'E'  && grid[yeniX][yeniY] != 'H' && grid[yeniX][yeniY] != 'D') {
                        yığın.push(new Konum(yeniX, yeniY));
                    }
                }
            }

            for (DinamikEngel engel : dinamikEngeller) {
                engel.hareketEt();
            }

            durumlar.add(gridKopyasınıAl(grid));
        }

        avcıPozisyonu = new Konum(satirlar - 1, sutunlar - 1);

    }

    private boolean geçerliMi(int x, int y) {
        return x >= 0 && x < satirlar && y >= 0 && y < sutunlar && grid[x][y] != 'Y';
    }

    private void dosyadanGridOku() {
        try {
            BufferedReader okuyucu = new BufferedReader(new FileReader("harita.txt"));
            ArrayList<String> satırlar = new ArrayList<>();
            String satır;
            while ((satır = okuyucu.readLine()) != null) {
                satırlar.add(satır);
            }
            satirlar = satırlar.size();
            sutunlar = satırlar.get(0).length();
            grid = new char[satirlar][sutunlar];

            for (int i = 0; i < satirlar; i++) {
                for (int j = 0; j < sutunlar; j++) {
                    grid[i][j] = satırlar.get(i).charAt(j);
                    if (grid[i][j] == 'A') {
                        gKonumlari.add(new Konum(i, j));
                    } else if (grid[i][j] == 'G') {
                        sKonumlari.add(new Konum(i, j));
                    } else if (grid[i][j] == 'E') {
                        yKonumlari.add(new Konum(i, j));
                    } else if (grid[i][j] == 'Y') {
                        mKonumlari.add(new Konum(i, j));
                    }
                    else if (grid[i][j] == 'D') {
                        DKonumlari.add(new Konum(i, j));
                    }
                }
            }

            okuyucu.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void etiketleriBaşlat() {
        setLayout(new GridLayout(satirlar, sutunlar));
        etiketler = new JLabel[satirlar][sutunlar];
        for (int i = 0; i < satirlar; i++) {
            for (int j = 0; j < sutunlar; j++) {
                etiketler[i][j] = new JLabel();
                etiketler[i][j].setPreferredSize(new Dimension(HÜCRE_BOYUTU, HÜCRE_BOYUTU));
                etiketler[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                etiketler[i][j].setHorizontalAlignment(SwingConstants.CENTER);
                etiketler[i][j].setVerticalAlignment(SwingConstants.CENTER);
                etiketler[i][j].setBackground(Color.black);
                etiketler[i][j].setOpaque(true);
                add(etiketler[i][j]);
            }
        }
    }

    private void etiketleriGüncelle() {
        char[][] mevcutDurum = durumlar.get(Main.mevcutDurum);
        k=0;
        for (int i = 0; i < satirlar; i++) {
            for (int j = 0; j < sutunlar; j++) {
                switch (mevcutDurum[i][j]) {
                    case '0':
                        etiketler[i][j].setText("");
                        etiketler[i][j].setBackground(Color.black);
                        break;
                    case 'A':
                        etiketler[i][j].setText("A");
                        etiketler[i][j].setForeground(SARI_RENK);
                        break;
                    case 'G':
                        etiketler[i][j].setText("G");
                        etiketler[i][j].setForeground(SARI_RENK);
                        break;
                    case 'E':
                        etiketler[i][j].setText("E");
                        etiketler[i][j].setForeground(YEŞIL_RENK);
                        break;
                    case 'M':
                        etiketler[i][j].setText("M");
                        etiketler[i][j].setForeground(YEŞIL_RENK);
                        break;
                    case 'D':
                        etiketler[i][j].setText("D");
                        etiketler[i][j].setBackground(Color.orange);
                        break;
                    case '-':
                        etiketler[i][j].setText("-");
                        etiketler[i][j].setBackground(Color.white);
                        break;
                    case 'F':
                        toplananHazineSayisi++;
                        etiketler[i][j].setText("F");
                        etiketler[i][j].setBackground(Color.pink);
                        for (char[][]durum:
                                durumlar) {
                            durum[i][j] = '-';
                        }
                        JOptionPane.showMessageDialog(null, "Hazine: (" + i + ", " + j + ")");
                        if(toplananHazineSayisi == keşfedilenHazine.size()){
                            JOptionPane.showMessageDialog(null,"Tüm Hazineler Bulundu!");
                            System.exit(1);
                        }
                        break;
                    default:
                        etiketler[i][j].setText(String.valueOf(mevcutDurum[i][j]));
                }

                if (mevcutDurum[i][j] == '.' || mevcutDurum[i][j] == 'F') {
                    etiketler[i][j].setBackground(Color.white);
                }
            }
        }

        if (avcıPozisyonu != null) {
            etiketler[avcıPozisyonu.x][avcıPozisyonu.y].setBackground(MAVI_RENK);
        }
    }

    private char[][] gridKopyasınıAl(char[][] orijinal) {
        char[][] kopya = new char[orijinal.length][orijinal[0].length];
        for (int i = 0; i < orijinal.length; i++) {
            System.arraycopy(orijinal[i], 0, kopya[i], 0, orijinal[0].length);
        }
        return kopya;
    }

    private void dinamikEngelEkle() {
        for (Konum konum : mKonumlari) {
            DinamikEngel engel = new DinamikEngel(konum, 3, 'x');
            dinamikEngeller.add(engel);
            grid[konum.getX()][konum.getY()] = 'H';
        }
        for (Konum konum : DKonumlari) {
            DinamikEngel engel = new DinamikEngel(konum, 3, 'y');
            dinamikEngeller.add(engel);
            grid[konum.getX()][konum.getY()] = 'H';
        }


    }
}

class KontrolPenceresi extends JFrame {
    public KontrolPenceresi() {
        setTitle("Kontrol Penceresi");
        setLayout(new FlowLayout());
        JButton anaPencereyiÇalıştırButonu = new JButton("Ana Pencereyi Çalıştır");
        anaPencereyiÇalıştırButonu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Main anaPencere = new Main();
                anaPencere.anaPencereyiBaşlat();
                dispose(); // Butona tıkladıktan sonra kontrol penceresini kapat
            }
        });
        JButton haritaOluştur = new JButton("Yeni bir harita oluştur");
        haritaOluştur.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Scanner tarayıcı = new Scanner(System.in);

                System.out.println("Satır sayısını girin:");
                int satırlar = tarayıcı.nextInt();
                System.out.println("Sütun sayısını girin:");
                int sutunlar = tarayıcı.nextInt();

                char[][] grid = new char[satırlar][sutunlar];
                ArrayList<Konum> gKonumları = new ArrayList<>();
                ArrayList<Konum> sKonumları = new ArrayList<>();
                ArrayList<Konum> yKonumları = new ArrayList<>();
                ArrayList<Konum> mKonumları = new ArrayList<>();
                ArrayList<Konum> DKonumları = new ArrayList<>();


                // Grid'i rastgele harflerle doldur
                Random rastgele = new Random();
                for (int i = 0; i < satırlar; i++) {
                    for (int j = 0; j < sutunlar; j++) {
                        grid[i][j] = '0';
                    }
                }


                int numG = 5;
                for (int i = 0; i < numG; i++) {
                    int x = rastgele.nextInt(satırlar);
                    int y = rastgele.nextInt(sutunlar);
                    grid[x][y] = 'A';
                    gKonumları.add(new Konum(x, y));
                }


                int numS = 5;
                for (int i = 0; i < numS; i++) {
                    int x = rastgele.nextInt(satırlar);
                    int y = rastgele.nextInt(sutunlar);
                    grid[x][y] = 'G';
                    sKonumları.add(new Konum(x, y));
                }

                for (int i = 0; i < satırlar; i++) {
                    for (int j = 0; j < sutunlar; j++) {
                        int engelŞansı = rastgele.nextInt(100);
                        if (engelŞansı < 3) { // %5 engel olasılığı
                            int engelBoyutu = rastgele.nextInt(4);
                            for (int k = i; k < Math.min(satırlar, i + engelBoyutu); k++) {
                                for (int l = j; l < Math.min(sutunlar, j + engelBoyutu); l++) {
                                    grid[k][l] = 'E';
                                    yKonumları.add(new Konum(k, l));
                                }
                            }
                        }
                    }
                }


                int numM = 4;
                for (int i = 0; i < numM; i++) {
                    int x = rastgele.nextInt(satırlar);
                    int y = rastgele.nextInt(sutunlar);
                    grid[x][y] = 'E';
                    mKonumları.add(new Konum(x, y));
                }
                int numV = 4;
                for (int i = 0; i < numV; i++) {
                    int x = rastgele.nextInt(satırlar);
                    int y = rastgele.nextInt(sutunlar);
                    grid[x][y] = 'D';
                    DKonumları.add(new Konum(x, y));
                }

                // Dosyaya yazdır
                try {
                    PrintWriter yazıcı = new PrintWriter(new FileWriter("harita.txt"));
                    for (int i = 0; i < satırlar; i++) {
                        for (int j = 0; j < sutunlar; j++) {
                            yazıcı.print(grid[i][j]);
                        }
                        yazıcı.println();
                    }
                    yazıcı.close();
                    System.out.println("Harita oluşturuldu ve dosyaya yazdırıldı!");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        add(anaPencereyiÇalıştırButonu);
        add(haritaOluştur);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
