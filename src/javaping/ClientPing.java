public class ClientPing {

    private static final int PING_SAYISI = 6; //gonderilecek toplam paket sayisi
    private static final int TIMEOUT = 1000; // 1 dakika

    public static void main(String[] args) throws Exception {

        Scanner klavye = new Scanner(System.in);
        String kullanicidanAlinanHostNo;
        System.out.print("Bir host(IP) numarası girin :");
        kullanicidanAlinanHostNo = klavye.nextLine();

        //InetAddress serverHostNumara = InetAddress.getByName("localhost");
        InetAddress serverHostNumara = InetAddress.getByName(kullanicidanAlinanHostNo);

        int portNumara = 25400;

        //UDP paketlerini almak ve göndermek için bir datagram soketi
        DatagramSocket datagramSoket = new DatagramSocket();

        //Soketin ilk acilis anında beklenmesi sağlandı.
        datagramSoket.setSoTimeout(TIMEOUT);

        int paketSiraNo = 0;
        int kaybolanPaketSayisi = 0;
        int alinanPaketSayisi = 0;
        double kaybolanPaketYuzdesi = 0;
        long baslangicZamanMs = 0; //istek paket gonderildiginde baslangıc zamanı
        long bitisZamanMs = 0; //cevap paket geldigindeki bitis zamanı
        long RTT = 0; //Paketin gidiş-dönus hesabı (round-trip times)

        for (paketSiraNo = 0; paketSiraNo < PING_SAYISI; paketSiraNo++) {
            //paket icin baslangic zamanını sistemden al
            baslangicZamanMs = System.currentTimeMillis();

            //istek mesajı olusturma
            String mesajMetni = "PING : paketin sıra numarası(paketSiraNo)= " + paketSiraNo + " | baslangic zaman= " + baslangicZamanMs + "";
            byte[] mesaj = mesajMetni.getBytes();

            //servera istek mesajı gondermek icin datagramPacket olustur
            DatagramPacket istekPaket = new DatagramPacket(mesaj, mesaj.length, serverHostNumara, portNumara);

            //Cevabı almak icin tekrar datagramPacket olustur
            DatagramPacket cevapPaket = new DatagramPacket(new byte[1024], 1024);

            //datagramPacket istegini , istekPaket i , soket aracılıgıyla host'a gonder
            datagramSoket.send(istekPaket);
            Thread.sleep(1000);

            try {
                //clientı bir UDP cevap paketi alana kadar beklemesi icin engelle  
                datagramSoket.receive(cevapPaket);
            } catch (Exception e) {
                // cevap paket gelmediyse
                System.err.println(paketSiraNo + " . paket(ping) icin cevap verilmedi");
                kaybolanPaketSayisi += 1;
                System.err.println("" + e.toString() + " (İstek  zaman asimina ugradi)");
                //Thread.sleep(2000);
            }
            //cevap paketi geldiyse
            bitisZamanMs = System.currentTimeMillis(); //Yanit geldigi zamanı sistemden al
            RTT = bitisZamanMs - baslangicZamanMs; // gidis - donus hesabı

            //Gonderilen istek paketin ekrana yazılma
            yazdirVeri(istekPaket);

            //Bitis zamani
            System.out.print(" |  bitis zaman=" + bitisZamanMs + "\n");

            //RTT ekrana yaz
            System.out.println("---> RTT = " + RTT + " ms \n");

        }

        //Clienta alınan paket sayısı
        alinanPaketSayisi = PING_SAYISI - kaybolanPaketSayisi;

        //paket kayip yuzdesi hesabı
        kaybolanPaketYuzdesi = ((double) kaybolanPaketSayisi / PING_SAYISI) * 100;

        //paketlerin son durumda bilgileri
        System.out.println("\n\n ---- " + serverHostNumara + " ---- \n\n");
        System.out.format(" %d : toplam paket(ping) \n %d : alinan paket sayisi \n %% %.1f  : kaybolan paket yuzdesi\n\n", PING_SAYISI, alinanPaketSayisi, kaybolanPaketYuzdesi);

        if (alinanPaketSayisi == 0) {
            System.out.println("Hic bir paket hedefe ulasmadi");
        }

        System.out.println("Output");
        System.out.format("[IP(Host) adresi : %s ] - [Toplam gonderilen paket sayisi : %d ] - [Servera ulasan paket sayısı : %d ] - [Kaybolan paket sayisi : %d ] \n", serverHostNumara, PING_SAYISI, alinanPaketSayisi, kaybolanPaketSayisi);
        //Soket baglantisini kapat
        datagramSoket.close();

    }

    //Standart çıktı akışına gore ping paketlerinina alınan byte verileri ekranda string'e donusturerek yazdırma
    private static void yazdirVeri(DatagramPacket cevapPaket) throws Exception {
        // Paketi bayt dizisine aktarma
        byte[] bufferAlani = cevapPaket.getData();

        //Bayt dizisini akış sırasında okumak icin sıra ile işleme fonk
        ByteArrayInputStream byteArrayAkisSirasi = new ByteArrayInputStream(bufferAlani);

        //Bayt dizisinin sırası belirlendikten sonra okunması icin karakter olması gerekir.
        //Byte -> karakter cevirimi icin
        InputStreamReader karakterAkisOkuyucu = new InputStreamReader(byteArrayAkisSirasi);

        //Karakter arabelleğinden satır satır okumak icin
        BufferedReader bufferReader = new BufferedReader(karakterAkisOkuyucu);

        //Veriler tek satırda oldugundan
        String satirOku = bufferReader.readLine();

        //Host adresi ve alinan veriler
        System.out.println(
                "İstek paket : "
                + cevapPaket.getAddress().getHostAddress() + "  |  Paket uzunlugu :" + cevapPaket.getLength()
                + "  |  : "
                + satirOku);
    }
}
