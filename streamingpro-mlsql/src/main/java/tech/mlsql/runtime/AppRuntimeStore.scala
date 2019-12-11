package tech.mlsql.runtime

import com.fasterxml.jackson.annotation.JsonIgnore
import tech.mlsql.app.CustomController
import tech.mlsql.runtime.kvstore.{InMemoryStore, KVIndex, KVStore}

/**
 * 6/11/2019 WilliamZhu(allwefantasy@gmail.com)
 */
class AppRuntimeStore(val store: KVStore, val listener: Option[AppSRuntimeListener] = None)
  extends ControllerRuntimeStore
    with LoadSaveRuntimeStore {

}

trait ControllerRuntimeStore {
  self: AppRuntimeStore =>
  def registerController(name: String, className: String) = {
    store.write(CustomClassItemWrapper(CustomClassItem(name, className)))
  }

  def removeController(name: String) = {
    store.delete(classOf[CustomClassItemWrapper], name)
  }

  def getController(name: String): Option[CustomClassItemWrapper] = {
    try {
      Some(store.read(classOf[CustomClassItemWrapper], name))
    } catch {
      case e: NoSuchElementException =>
        None
      case e: Exception => throw e
    }

  }

}

trait LoadSaveRuntimeStore {
  self: AppRuntimeStore =>

  def registerLoadSave(name: String, className: String) = {
    val wrapper = getLoadSave(name) match {
      case Some(item) =>
        val customClassItems = item.customClassItems.copy(classNames = (item.customClassItems.classNames ++ Seq(className)))
        item.copy(customClassItems = customClassItems)
      case None =>
        CustomClassItemListWrapper(CustomClassItemList(name, Seq(className)))
    }
    store.write(wrapper)
  }

  def removeLoadSave(name: String) = {
    store.delete(classOf[CustomClassItemWrapper], name)
  }

  def getLoadSave(name: String): Option[CustomClassItemListWrapper] = {
    try {
      Some(store.read(classOf[CustomClassItemListWrapper], name))
    } catch {
      case e: NoSuchElementException =>
        None
      case e: Exception => throw e
    }

  }
}

object AppRuntimeStore {
  private val _store = new InMemoryStore()
  val store = new AppRuntimeStore(_store)
  val LOAD_SAVE_KEY = "load_save_key"
}

class Jack extends CustomController {
  override def run(params: Map[String, String]): String = {
    "[]"
  }
}

class AppSRuntimeListener {}

case class CustomClassItemWrapper(customClassItem: CustomClassItem) {
  @JsonIgnore
  @KVIndex
  def id = customClassItem.name
}

case class CustomClassItem(@KVIndex name: String, className: String)

case class CustomClassItemListWrapper(customClassItems: CustomClassItemList) {
  @JsonIgnore
  @KVIndex
  def id = customClassItems.name
}

case class CustomClassItemList(@KVIndex name: String, classNames: Seq[String])
