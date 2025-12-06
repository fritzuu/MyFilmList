# MyMovieLog - Java Movie Tracker App

MyMovieLog adalah aplikasi desktop berbasis Java GUI yang memungkinkan pengguna untuk mencari film, melihat detail, dan menyimpannya ke dalam daftar tontonan (Watchlist) pribadi.

Aplikasi ini dibangun menggunakan **Java Swing** dan terintegrasi dengan **TMDB (The Movie Database) API** untuk mendapatkan data film secara *real-time*.

## Fitur Utama
* **Pencarian Film:** Mencari judul film menggunakan data langsung dari TMDB API.
* **Detail Film:** Menampilkan sinopsis, tanggal rilis, rating, dan poster film.
* **Watchlist:** Menyimpan film yang ingin ditonton ke dalam daftar pribadi.
* **Persistensi Data:** Data Watchlist tersimpan secara permanen di komputer lokal (tidak hilang saat aplikasi ditutup).
* **Async Loading:** Proses pengambilan data dan gambar berjalan di latar belakang tanpa membekukan antarmuka (GUI).
* **Menggunakan API:** Menggunakan API dari [The Movie Database (TMDB)](https://www.themoviedb.org/documentation/api) sebagai sumber database film.

## Konsep Pemrograman yang Diterapkan
Proyek ini dibuat untuk mendemonstrasikan pemahaman mendalam tentang materi inti Java tanpa ketergantungan pada library pihak ketiga (seperti Gson atau Jackson):

1.  **Java GUI (Swing):** Menggunakan `JFrame`, `BorderLayout`, `JSplitPane`, dan `JList` untuk antarmuka yang responsif.
2.  **Networking & API:** Menggunakan `HttpURLConnection` untuk melakukan request GET ke endpoint REST API.
3.  **Advanced String Manipulation:** Melakukan *parsing* data JSON secara manual menggunakan manipulasi `String` dan `Regex` (tanpa library JSON external).
4.  **Multithreading:** Mengimplementasikan `Thread` dan `SwingUtilities.invokeLater` untuk menangani proses I/O (network request) agar tidak memblokir *Main Thread*.
5.  **Java I/O & Serialization:** Menggunakan `ObjectOutputStream` dan `Serializable` untuk menyimpan objek Watchlist ke file lokal (`.dat`).
6.  **Collections & Generics:** Penggunaan `ArrayList<Movie>` dan `DefaultListModel` untuk manajemen data yang efisien.
7.  **Exception Handling:** Penanganan error yang robust untuk koneksi jaringan dan operasi file.

## Persyaratan Sistem
* Java Development Kit (JDK) 8 atau lebih baru.
* Koneksi Internet (untuk mengambil data API).

Project PBO Kelompok 11
Angota:
1. L0124035 - Agischa Nur Aghianingtyas
2. L0124144 - Wantech Arofiq Huda F.       
3. L0124154 - Revan Alifian Zhafran        
