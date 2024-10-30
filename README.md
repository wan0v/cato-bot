<p align="center">
    <img src="https://media.nocars.tk/cato-logo.png" alt="logo" width="100"/>
</p>
<p align="center">
    Cato-Bot is a teamspeak bot, that can edit channel descriptions in a given interval. You can either provide a xml source (rss) or fetch a website with css-selectors.
</p>
<p align="center">
    <img src="https://media.nocars.tk/cato-screenshot.png" alt="logo" width="600"/>
</p>

# Install
1. Grab the latest files from the release section.
2. Unpack the folder and place it on the desired location.
3. Edit `config.yaml` and change the following parameters:
    - `ipAddress`
    - `username`
    - `password`
4. Run `java -jar ./cato-bot-0.3.jar`. At least JDK 22 or higher is required.
5. The bot should make itself noticeable with a message in the default channel.

If you **don't have Java** installed or **prefer** to run the bot as a **docker container**:\
Follow the instructions up to step 4. Then run:\
_Important: Change the -v paths to match the paths on your machine._
```
sudo docker run -d --name cato-bot -v /path/to/cato-bot/config.yaml:/config.yaml -v /path/to/cato-bot/news/:/news wanov/cato-bot:latest
```

# Command Overview
## Feed configuration
List all available channels and their ids:
```
!cid
```
Add a rss feed (xml) to a given channel id:
```
!add CID RSS-URL
```
### !addhtml
Fetch the html of a website with defined css-selectors. For examples have a look at the [!addhtml Boilerplate](#addhtml-boilerplate):
```
!addhtml CID URL cssPARENT cssTITLE cssLINK cssDESCRIPTION cssDATE
```
Optional parameters (replace with 0):
- CSS-PARENT
- CSS-TITLE
- CSS-DATE

Remove a feed from a given channel id:
```
!rm CID
```
Manually refresh your feeds once:
```
!refresh
```

## Backups
A backup of the feed configuration is made automatically on !shutdown.
Do a manual backup:
```
!makebackup
```
List all available backup files:
```
!listbackup
```
Print the content of a given backup file:
```
!printbackup FILENAME
```
Load a given backup file, if it exists:
```
!loadbackup FILENAME
```

## Stop the bot
Disconnect the bot from the server, disable any channel updates and remove the feeds from all channel descriptions. A backup of the last feed configuration will be made
```
!shutdown
```

# !addhtml Boilerplate
## 9to5mac.com
```
!addhtml CID https://9to5mac.com/ article.article.standard h2.h1 h2.h1>a div.article__content>div.article__excerpt>p div.post-meta>span.meta__post-date
```
## guildwars2.com News
```
!addhtml CID https://www.guildwars2.com/en/news/ li.blog-post h3.blog-title h3.blog-title>a div.text>p:first-child p.blog-attribution
```
## swtorista.com
```
!addhtml CID https://swtorista.com/ .news-card .title .title p 0
```
## Hunt Showdown News
```
!addhtml CID https://www.huntshowdown.com/news .news-feature-text a>h3 a p h5>a>span
```
