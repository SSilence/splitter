## Splitter App

Copyright 2019 Tobias Zeising, tobias.zeising@aditu.de  
https://www.lesestunden.de
Licensed under the GPLv3 license

CRAWLER
-------

For scanning the splitter-verlag.de page for comics start spring boot application in `/crawler` subdir. Use -Xmx10g in VM options. Use program argument `--crawl=/your/target/dir/books.json` for crawling the page and saving all books metadata in target dir.


APP
---

Open subdir `/app` with Android Studio for editing and building the Android App.


CREDITS
-------

Special thanks to the great programmers of this libraries which will be used:

* [Kotlin](https://kotlinlang.org)
* [RxJava](https://github.com/ReactiveX/RxJava)
* [OkHttp](http://square.github.io/okhttp/)
* [Retrofit](http://square.github.io/retrofit)
* [Koin](https://insert-koin.io)
* [Android Jetpack](https://developer.android.com/jetpack)
* [StfalconImageViewer](https://github.com/stfalcon-studio/StfalconImageViewer)
* [AlphabetIndex-Fast-Scroll-RecyclerView](https://github.com/myinnos/AlphabetIndex-Fast-Scroll-RecyclerView)