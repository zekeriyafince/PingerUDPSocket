public class ServerPing {

    private static final double KAYIP_ORAN = 0.3; //Her paket cevap gondermemek icin kayıp
    private static final int ORTALAMA_GECIKME = 200;

    public static void main(String[] args) throws Exception {
        //İletisimin saglanması icin gereken port numarası
        int portNumara;
        portNumara = 25400;

        //paket kaybi ve ağ gecikmesini bilmek icin rastgele sayi uret
        Random randomSayi = new Random();

        //UDP paketlerini belirtilen port numarası üzerinden almak/gondermek icin DatagramSocket nesnesi
        DatagramSocket datagramSoket = null;
        try {
            datagramSoket = new DatagramSocket(portNumara);
            System.out.println("Server dinlemeye basladi\n");

            while (true) {
                //Gelen UDP paketini tutmak icin DatagramPacketini belli bir bellek alanı gerekli
                DatagramPacket istekPaket = new DatagramPacket(
                        new byte[1024], //bellek alani 
                        1024); //bellek alanı uzunlugu

                //Hostu bir UDP paketi alana kadar dinlemesi icin
                datagramSoket.receive(istekPaket);
                //Paketi aldıktan sonra devam eder islemlere
                System.out.println("Server paket aldi..");

                //Alınan verileri yazdirmak icin fonksiyona gonder
                yazdirVeri(istekPaket);

                Thread.sleep(500);

                //Paket kaybi icin yanıtlama durumu kontrolu
                if (randomSayi.nextDouble() < KAYIP_ORAN) {
                    System.err.println("Yanit gonerilemedi\n");

                    continue;
                } else {
                    //Ağ gecikmesi ayarı
                    Thread.sleep((int) (randomSayi.nextDouble() * 2 * ORTALAMA_GECIKME));

                    //Cevap gonderme
                    InetAddress clientHost = istekPaket.getAddress();
                    int clientPort = istekPaket.getPort();
                    byte[] buffer = istekPaket.getData();
                    DatagramPacket yanitPaket = new DatagramPacket(buffer, buffer.length, clientHost, clientPort);
                    datagramSoket.send(yanitPaket);
                    System.out.println("Yanit gonderildi  \n");

                }

            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (datagramSoket != null) {
                datagramSoket.close();
            }
        }

    }

    //Standart çıktı akışına gore ping paketlerinina alınan byte verileri ekranda string'e donusturerek yazdırma
    private static void yazdirVeri(DatagramPacket istekPaket) throws Exception {
        // Paketi bayt dizisine aktarma
        byte[] bufferAlani = istekPaket.getData();

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
                "Alinan paket : "
                + istekPaket.getAddress().getHostAddress() + "  |  Paket uzunlugu :" + istekPaket.getLength()
                + "  |  : "
                + satirOku + "");
    }

}
