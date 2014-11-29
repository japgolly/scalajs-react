package japgolly.scalajs.react.example.examples

import japgolly.scalajs.react.vdom.ReactVDom.ReactVExt_Attr
import japgolly.scalajs.react.vdom.ReactVDom.all._
import japgolly.scalajs.react.{BackendScope, ReactComponentB}

import scala.scalajs.js

/**
 * Created by chandrasekharkode on 11/18/14.
 */
object PictureAppExample {

  val pictureJsxCode = """
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
                         |React.render(
                         |    <PictureList apiKey="642176ece1e7445e99244cec26f4de1f" />,
                         |    document.body
                         |);
                         | """.stripMargin


  val pictureScalaCode = """
                           |case class Picture(id : String, url : String, src : String, title : String, favorite : Boolean = false )
                           |
                           |  case class State(pictures : List[Picture] , favourites : List[Picture])
                           |
                           |  class  Backend( t: BackendScope[Unit,State])  {
                           |
                           |    def onPicClick(id : String , favorite : Boolean) = {
                           |      if(favorite) {
                           |         onFavClick(id,favorite)
                           |      } else {
                           |        var newPic : Picture = null
                           |        val newPics = t.state.pictures.map(p => if(p.id == id) { newPic = p.copy(favorite = true); newPic} else p )
                           |        val newFavs = t.state.favourites.+:(newPic)
                           |        t.modState(_ => State(newPics,newFavs))
                           |      }
                           |
                           |    }
                           |    def onFavClick(id : String , favorite : Boolean) = {
                           |      val newPics = t.state.pictures.map(p => if(p.id == id) p.copy(favorite = false) else p )
                           |      val newFavs = t.state.favourites.filter(p => p.id != id)
                           |      t.modState(_ => State(newPics,newFavs))
                           |    }
                           |
                           |  }
                           |  val picture = ReactComponentB[(Picture,Function2[String,Boolean,Unit])]("picture")
                           |    .render( P => {
                           |        val (p,b) = P
                           |       div( if(p.favorite) cls := "picture favorite" else cls := "picture" , onclick --> b(p.id,p.favorite) )(
                           |         img( src := p.src , title := p.title )
                           |       )
                           |      })
                           |    .build
                           |
                           |  val pictureList = ReactComponentB[(List[Picture],Function2[String,Boolean,Unit])]("pictureList")
                           |   .render(P => {
                           |       val (list,b) = P
                           |      div(`class` := "pictures")(
                           |        if(list.isEmpty) span("Loading Pics..")
                           |        else {
                           |          list.map(p => picture.withKey(p.id)((p,b)))
                           |        }
                           |      )
                           |     })
                           |   .build
                           |
                           |  val favoriteList = ReactComponentB[(List[Picture],Function2[String,Boolean,Unit])]("favoriteList")
                           |   .render(P => {
                           |    val (list,b) = P
                           |    div(`class` := "favorites")(
                           |        if(list.isEmpty) span("Click an image to mark as  favorite")
                           |        else {
                           |         list.map(p => picture.withKey(p.id)((p,b)))
                           |        }
                           |      )
                           |     })
                           |   .build
                           |
                           |  val PictureApp = ReactComponentB[Unit]("PictureApp")
                           |    .initialState(State(Nil,Nil))
                           |    .backend(new Backend(_))
                           |    .render((_,S,B) => {
                           |       div(
                           |         h1("Popular Instragram pics"),
                           |         pictureList((S.pictures,B.onPicClick)),
                           |         h1("Your favorites"),
                           |         favoriteList((S.favourites,B.onFavClick))
                           |       )
                           |      })
                           |    .componentDidMount(scope => {
                           |       // make ajax call here to get pics from instagram
                           |        import  scalajs.js.Dynamic.{global => g}
                           |        val url = "https://api.instagram.com/v1/media/popular?client_id=642176ece1e7445e99244cec26f4de1f&callback=?"
                           |        g.jsonp(url, (result : js.Dynamic) => {
                           |          if(result != js.undefined && result.data != js.undefined ) {
                           |            val data = result.data.asInstanceOf[js.Array[js.Dynamic]]
                           |            val pics =   data.toList.map(item => Picture(item.id.toString, item.link.toString ,item.images.low_resolution.url.toString, if(item.caption != null) item.caption.text.toString else ""))
                           |            scope.modState(_ =>State(pics,Nil))
                           |          }
                           |        })
                           |     })
                           |    .buildU""".stripMargin


  case class Picture(id: String, url: String, src: String, title: String, favorite: Boolean = false)

  case class State(pictures: List[Picture], favourites: List[Picture])

  class Backend(t: BackendScope[Unit, State]) {

    def onPicClick(id: String, favorite: Boolean) = {
      if (favorite) {
        val newPics = t.state.pictures.map(p => if (p.id == id) p.copy(favorite = false) else p)
        val newFavs = t.state.favourites.filter(p => p.id != id)
        t.modState(_ => State(newPics, newFavs))
      } else {
        var newPic: Picture = null
        val newPics = t.state.pictures.map(p => if (p.id == id) {
          newPic = p.copy(favorite = true); newPic
        } else p)
        val newFavs = t.state.favourites.+:(newPic)
        t.modState(_ => State(newPics, newFavs))
      }
    }
  }

  val picture = ReactComponentB[(Picture, Function2[String, Boolean, Unit])]("picture")
    .render(P => {
    val (p, b) = P
    div(if (p.favorite) cls := "picture favorite" else cls := "picture", onclick --> b(p.id, p.favorite))(
      img(src := p.src, title := p.title)
    )
  })
    .build

  val pictureList = ReactComponentB[(List[Picture], Function2[String, Boolean, Unit])]("pictureList")
    .render(P => {
    val (list, b) = P
    div(`class` := "pictures")(
      if (list.isEmpty) span("Loading Pics..")
      else {
        list.map(p => picture.withKey(p.id)((p, b)))
      }
    )
  })
    .build

  val favoriteList = ReactComponentB[(List[Picture], Function2[String, Boolean, Unit])]("favoriteList")
    .render(P => {
    val (list, b) = P
    div(`class` := "favorites")(
      if (list.isEmpty) span("Click an image to mark as  favorite")
      else {
        list.map(p => picture.withKey(p.id)((p, b)))
      }
    )
  })
    .build

  val PictureApp = ReactComponentB[Unit]("PictureApp")
    .initialState(State(Nil, Nil))
    .backend(new Backend(_))
    .render((_, S, B) => {
        div(
          h1("Popular Instagram Pics"),
          pictureList((S.pictures, B.onPicClick)),
          h1("Your favorites"),
          favoriteList((S.favourites, B.onPicClick))
        )
      })
    .componentDidMount(scope => {
    // make ajax call here to get pics from instagram
        import scalajs.js.Dynamic.{global => g}
        val url = "https://api.instagram.com/v1/media/popular?client_id=642176ece1e7445e99244cec26f4de1f&callback=?"
        g.jsonp(url, (result: js.Dynamic) => {
          if (result != js.undefined && result.data != js.undefined) {
            val data = result.data.asInstanceOf[js.Array[js.Dynamic]]
            val pics = data.toList.map(item => Picture(item.id.toString, item.link.toString, item.images.low_resolution.url.toString, if (item.caption != null) item.caption.text.toString else ""))
            scope.modState(_ => State(pics, Nil))
          }
        })
      })
    .buildU

}
