package ghpages.examples

import ghpages.{GhPagesMacros, Jsonp}
import japgolly.scalajs.react._
import vdom.all._

import scala.scalajs.js
import ghpages.examples.util.SideBySide

/**
 * Created by chandrasekharkode on 11/18/14.
 */
object PictureAppExample {

  def content = SideBySide.Content(pictureJsxCode, pictureScalaCode, PictureApp())

  val pictureJsxCode =
    """
      |/** @jsx React.DOM */
      |var Picture = React.createClass({
      |    clickHandler: function(){
      |        this.props.onClick(this.props.ref);
      |    },
      |    render: function(){
      |        var cls = 'picture ' + (this.props.favorite ? 'favorite' : '');
      |        return (
      |            <div className={cls} onClick={this.clickHandler}>
      |                <img src={this.props.src} width="200" title={this.props.title} />
      |            </div>
      |        );
      |    }
      |});
      |
      |var PictureList = React.createClass({
      |
      |    getInitialState: function(){
      |
      |        return { pictures: [], favorites: [] };
      |    },
      |    componentDidMount: function(){
      |        var self = this;
      |        var url = 'https://api.instagram.com/v1/media/popular?client_id=' + this.props.apiKey + '&callback=?';
      |        $.getJSON(url, function(result){
      |            if(!result || !result.data || !result.data.length){
      |                return;
      |            }
      |            var pictures = result.data.map(function(p){
      |                return {
      |                    id: p.id,
      |                    url: p.link,
      |                    src: p.images.low_resolution.url,
      |                    title: p.caption ? p.caption.text : '',
      |                    favorite: false
      |                };
      |            });
      |            self.setState({ pictures: pictures });
      |        });
      |    },
      |
      |    pictureClick: function(id){
      |        var favorites = this.state.favorites,
      |            pictures = this.state.pictures;
      |        for(var i = 0; i < pictures.length; i++){
      |            if(pictures[i].id == id) {
      |                if(pictures[i].favorite){
      |                    return this.favoriteClick(id);
      |                }
      |                favorites.push(pictures[i]);
      |                pictures[i].favorite = true;
      |
      |                break;
      |            }
      |        }
      |        this.setState({pictures: pictures, favorites: favorites});
      |    },
      |
      |    favoriteClick: function(id){
      |        var favorites = this.state.favorites,
      |            pictures = this.state.pictures;
      |        for(var i = 0; i < favorites.length; i++){
      |            if(favorites[i].id == id) break;
      |        }
      |        favorites.splice(i, 1);
      |        for(i = 0; i < pictures.length; i++){
      |            if(pictures[i].id == id) {
      |                pictures[i].favorite = false;
      |                break;
      |            }
      |        }
      |        this.setState({pictures: pictures, favorites: favorites});
      |    },
      |
      |    render: function() {
      |        var self = this;
      |        var pictures = this.state.pictures.map(function(p){
      |            return <Picture ref={p.id} src={p.src} title={p.title} favorite={p.favorite} onClick={self.pictureClick} />
      |        });
      |        if(!pictures.length){
      |            pictures = <p>Loading images..</p>;
      |        }
      |        var favorites = this.state.favorites.map(function(p){
      |            return <Picture ref={p.id} src={p.src} title={p.title} favorite={true} onClick={self.favoriteClick} />
      |        });
      |
      |        if(!favorites.length){
      |            favorites = <p>Click an image to mark it as a favorite.</p>;
      |        }
      |        return (
      |            <div>
      |                <h1>Popular Instagram pics</h1>
      |                <div className="pictures"> {pictures} </div>
      |
      |                <h1>Your favorites</h1>
      |                <div className="favorites"> {favorites} </div>
      |            </div>
      |
      |        );
      |    }
      |});
      |
      |ReactDOM.render(
      |    <PictureList apiKey="642176ece1e7445e99244cec26f4de1f" />,
      |    document.body
      |);
      |""".stripMargin

  val pictureScalaCode = GhPagesMacros.exampleSource

  // EXAMPLE:START

  case class Picture(id: String, url: String, src: String, title: String, favorite: Boolean = false)

  case class State(pictures: List[Picture], favourites: List[Picture])

  type PicClick = (String, Boolean) => Callback

  class Backend($: BackendScope[Unit, State]) {

    def onPicClick(id: String, favorite: Boolean) =
      $.state flatMap { s =>
        if (favorite) {
          val newPics = s.pictures.map(p => if (p.id == id) p.copy(favorite = false) else p)
          val newFavs = s.favourites.filter(p => p.id != id)
          $.modState(_ => State(newPics, newFavs))
        } else {
          var newPic: Picture = null
          val newPics = s.pictures.map(p => if (p.id == id) {
            newPic = p.copy(favorite = true); newPic
          } else p)
          val newFavs = s.favourites.+:(newPic)
          $.modState(_ => State(newPics, newFavs))
        }
      }

    def render(s: State) =
      div(
        h1("Popular Instagram Pics"),
        pictureList((s.pictures, onPicClick)),
        h1("Your favorites"),
        favoriteList((s.favourites, onPicClick)))
  }

  val picture = ReactComponentB[(Picture, PicClick)]("picture")
    .render_P { case (p, b) =>
      div(if (p.favorite) cls := "picture favorite" else cls := "picture", onClick --> b(p.id, p.favorite))(
        img(src := p.src, title := p.title)
      )
    }
    .build

  val pictureList = ReactComponentB[(List[Picture], PicClick)]("pictureList")
    .render_P { case (list, b) =>
      div(`class` := "pictures")(
        if (list.isEmpty) span("Loading Pics..")
        else {
          list.map(p => picture.withKey(p.id)((p, b)))
        }
      )
    }
    .build

  val favoriteList = ReactComponentB[(List[Picture], PicClick)]("favoriteList")
    .render_P { case (list, b) =>
      div(`class` := "favorites")(
        if (list.isEmpty) span("Click an image to mark as  favorite")
        else {
          list.map(p => picture.withKey(p.id)((p, b)))
        }
      )
    }
    .build

  val PictureApp = ReactComponentB[Unit]("PictureApp")
    .initialState(State(Nil, Nil))
    .renderBackend[Backend]
    .componentDidMount(scope => Callback {
      // make ajax call here to get pics from instagram
      import scalajs.js.Dynamic.{global => g}
      def isDefined(g: js.Dynamic): Boolean =
        g.asInstanceOf[js.UndefOr[AnyRef]].isDefined
      val url = "https://api.instagram.com/v1/media/popular?client_id=642176ece1e7445e99244cec26f4de1f"
      Jsonp(url, (result: js.Dynamic) => {
        if (isDefined(result) && isDefined(result.data)) {
          val data = result.data.asInstanceOf[js.Array[js.Dynamic]]
          val pics = data.toList.map(item => Picture(item.id.toString, item.link.toString, item.images.low_resolution.url.toString, if (item.caption != null) item.caption.text.toString else ""))
          scope.modState(_ => State(pics, Nil)).runNow()
        }
      })
    })
    .build

  // EXAMPLE:END
}
