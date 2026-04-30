package com.github.schaka.janitorr.setup

import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path

private val log = LoggerFactory.getLogger(MediaLibrarySetup::class.java)

private val VIDEO_URLS = listOf(
    "https://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4",
)

private data class Movie(val title: String, val year: Int, val imdbId: String, val videoIndex: Int)

private data class Episode(val season: Int, val episode: Int, val title: String)

private data class TvShow(val title: String, val year: Int, val tvdbId: String, val episodes: List<Episode>, val videoIndex: Int)

private val MOVIES = listOf(
    Movie("Star Wars", 1977, "tt0076759", 0),
    Movie("The Empire Strikes Back", 1980, "tt0080684", 0),
    Movie("Return of the Jedi", 1983, "tt0086190", 0),
    Movie("The Lord of the Rings The Fellowship of the Ring", 2001, "tt0120737", 0),
    Movie("The Lord of the Rings The Two Towers", 2002, "tt0167261", 0),
    Movie("The Lord of the Rings The Return of the King", 2003, "tt0167260", 0),
    Movie("The Dark Knight", 2008, "tt0468569", 0),
    Movie("Inception", 2010, "tt1375666", 0),
    Movie("The Matrix", 1999, "tt0133093", 0),
    Movie("Forrest Gump", 1994, "tt0109830", 0),
)

private val TV_SHOWS = listOf(
    TvShow(
        title = "Breaking Bad", year = 2008, tvdbId = "81189", videoIndex = 0,
        episodes = listOf(
            Episode(1, 1, "Pilot"), Episode(1, 2, "Cats in the Bag"), Episode(1, 3, "And the Bags in the River"),
            Episode(2, 1, "Seven Thirty-Seven"), Episode(2, 2, "Down"), Episode(2, 3, "Bit by a Dead Bee"),
            Episode(3, 1, "No Mas"), Episode(3, 2, "Caballo Sin Nombre"), Episode(3, 3, "IFT"),
            Episode(4, 1, "Box Cutter"), Episode(4, 2, "Thirty-Eight Snub"), Episode(4, 3, "Open House"),
        )
    ),
    TvShow(
        title = "Friends", year = 1994, tvdbId = "79168", videoIndex = 0,
        episodes = listOf(
            Episode(1, 1, "The One Where Monica Gets a Roommate"), Episode(1, 2, "The One with the Sonogram at the End"), Episode(1, 3, "The One with the Thumb"),
            Episode(2, 1, "The One with Ross New Girlfriend"), Episode(2, 2, "The One with the Breast Milk"), Episode(2, 3, "The One Where Heckles Dies"),
            Episode(3, 1, "The One with the Princess Leia Fantasy"), Episode(3, 2, "The One Where No Ones Ready"), Episode(3, 3, "The One with the Jam"),
        )
    ),
    TvShow(
        title = "Game of Thrones", year = 2011, tvdbId = "121361", videoIndex = 0,
        episodes = listOf(
            Episode(1, 1, "Winter Is Coming"), Episode(1, 2, "The Kingsroad"), Episode(1, 3, "Lord Snow"),
            Episode(2, 1, "The North Remembers"), Episode(2, 2, "The Night Lands"), Episode(2, 3, "What Is Dead May Never Die"),
        )
    ),
    TvShow(
        title = "The Office", year = 2005, tvdbId = "73244", videoIndex = 0,
        episodes = listOf(
            Episode(1, 1, "Pilot"), Episode(1, 2, "Diversity Day"), Episode(1, 3, "Health Care"),
            Episode(2, 1, "The Dundies"), Episode(2, 2, "Sexual Harassment"), Episode(2, 3, "Office Olympics"),
        )
    ),
    TvShow(
        title = "Stranger Things", year = 2016, tvdbId = "305074", videoIndex = 0,
        episodes = listOf(
            Episode(1, 1, "The Vanishing of Will Byers"),
            Episode(1, 2, "The Weirdo on Maple Street"),
            Episode(1, 3, "Holly Jolly"),
            Episode(1, 4, "The Body"),
        )
    ),
)

class MediaLibrarySetup(private val mediaRoot: Path) {

    private val cacheDir = mediaRoot.resolve("cache")
    private val moviesDir = mediaRoot.resolve("movies")
    private val tvShowsDir = mediaRoot.resolve("TV Shows")

    fun prepare() {
        Files.createDirectories(cacheDir)
        downloadCacheFiles()

        deleteDirectory(moviesDir)
        deleteDirectory(tvShowsDir)
        Files.createDirectories(moviesDir)
        Files.createDirectories(tvShowsDir)

        createMovieStructure()
        createTvShowStructure()
    }

    private fun downloadCacheFiles() {
        val http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build()

        VIDEO_URLS.forEachIndexed { index, url ->
            val filename = url.substringAfterLast("/")
            val target = cacheDir.resolve(filename)

            if (Files.exists(target) && Files.size(target) > 1_000_000) {
                log.info("Cache hit [{}/{}]: {} ({} MB)", index + 1, VIDEO_URLS.size, filename, Files.size(target) / 1_048_576)
                return@forEachIndexed
            }

            log.info("Downloading [{}/{}]: {}", index + 1, VIDEO_URLS.size, filename)
            try {
                val request = HttpRequest.newBuilder(URI.create(url)).GET().build()
                val response = http.send(request, HttpResponse.BodyHandlers.ofFile(target))
                if (response.statusCode() !in 200..299 || Files.size(target) < 1_000_000) {
                    log.warn(
                        "Download failed for {} — HTTP {} ({} bytes), removing",
                        filename, response.statusCode(), Files.size(target)
                    )
                    Files.deleteIfExists(target)
                } else {
                    log.info("Downloaded: {} ({} MB)", filename, Files.size(target) / 1_048_576)
                }
            } catch (e: Exception) {
                log.warn("Failed to download {}: {}", filename, e.message)
                Files.deleteIfExists(target)
            }
        }
    }

    private fun deleteDirectory(dir: Path) {
        if (!Files.exists(dir)) return
        Files.walk(dir)
            .sorted(Comparator.reverseOrder())
            .forEach(Files::delete)
    }

    private fun createMovieStructure() {
        for (movie in MOVIES) {
            val sourceUrl = VIDEO_URLS[movie.videoIndex]
            val cacheName = sourceUrl.substringAfterLast("/")
            val cacheFile = cacheDir.resolve(cacheName)
            if (!Files.exists(cacheFile)) continue

            val dir = moviesDir.resolve("${movie.title} (${movie.year}) [imdbid-${movie.imdbId}]")
            Files.createDirectories(dir)

            val linkPath = dir.resolve("${movie.title} (${movie.year}).mp4")
            val target = dir.relativize(cacheFile)
            Files.createSymbolicLink(linkPath, target)
        }
    }

    private fun createTvShowStructure() {
        for (show in TV_SHOWS) {
            val sourceUrl = VIDEO_URLS[show.videoIndex]
            val cacheName = sourceUrl.substringAfterLast("/")
            val cacheFile = cacheDir.resolve(cacheName)
            if (!Files.exists(cacheFile)) continue

            val seasonNumbers = show.episodes.map { it.season }.distinct()
            for (seasonNum in seasonNumbers) {
                val seasonDir = tvShowsDir
                    .resolve("${show.title} (${show.year}) [tvdbid-${show.tvdbId}]")
                    .resolve("Season %02d".format(seasonNum))
                Files.createDirectories(seasonDir)

                for (ep in show.episodes.filter { it.season == seasonNum }) {
                    val filename = "${show.title} - S%02dE%02d - ${ep.title}.mp4"
                        .format(ep.season, ep.episode)
                    val linkPath = seasonDir.resolve(filename)
                    val target = seasonDir.relativize(cacheFile)
                    Files.createSymbolicLink(linkPath, target)
                }
            }
        }
    }
}
