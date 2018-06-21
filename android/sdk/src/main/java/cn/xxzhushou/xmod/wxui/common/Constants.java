/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cn.xxzhushou.xmod.wxui.common;

public class Constants {

  public interface Orientation {

    int HORIZONTAL = 0;
    int VERTICAL = 1;
  }

  public interface Weex {
    String REF = "ref";
    String INSTANCEID = "instanceid";
    String TYPE = "type";
  }

  public interface Name {

    String DEFAULT_WIDTH = "default-width";
    String DEFAULT_HEIGHT = "default-height";
    String HREF = "href";
    String WIDTH = "width";
    String MIN_WIDTH = "min-width";
    String MAX_WIDTH = "max-width";
    String HEIGHT = "height";
    String MIN_HEIGHT = "min-height";
    String MAX_HEIGHT = "max-height";
    String ALIGN_ITEMS = "align-items";
    String ALIGN_SELF = "align-self";
    String FLEX = "flex";
    String FLEX_DIRECTION = "flex-direction";
    String JUSTIFY_CONTENT = "justify-content";
    String FLEX_WRAP = "flex-wrap";

    String MARGIN = "margin";
    String MARGIN_TOP = "margin-top";
    String MARGIN_LEFT = "margin-left";
    String MARGIN_RIGHT = "margin-right";
    String MARGIN_BOTTOM = "margin-bottom";
    String PADDING = "padding";
    String PADDING_TOP = "padding-top";
    String PADDING_LEFT = "padding-left";
    String PADDING_RIGHT = "padding-right";
    String PADDING_BOTTOM = "padding-bottom";

    String LEFT = "left";
    String TOP = "top";
    String RIGHT = "right";
    String BOTTOM = "bottom";


    String BACKGROUND_COLOR = "background-color";
    String BACKGROUND_IMAGE = "background-image";
    String OPACITY = "opacity";
    String BORDER_RADIUS = "border-radius";
    String BORDER_WIDTH = "border-width";
    String BORDER_COLOR = "border-color";
    String BORDER_STYLE = "border-style";
    String BORDER_TOP_WIDTH = "border-top-width";
    String BORDER_RIGHT_WIDTH = "border-right-width";
    String BORDER_BOTTOM_WIDTH = "border-bottom-width";
    String BORDER_LEFT_WIDTH = "border-left-width";
    String BORDER_TOP_COLOR = "border-top-color";
    String BORDER_RIGHT_COLOR = "border-right-color";
    String BORDER_BOTTOM_COLOR = "border-bottom-color";
    String BORDER_LEFT_COLOR = "border-left-color";
    String BORDER_TOP_LEFT_RADIUS = "border-top-left-radius";
    String BORDER_TOP_RIGHT_RADIUS = "border-top-right-radius";
    String BORDER_BOTTOM_RIGHT_RADIUS = "border-bottom-right-radius";
    String BORDER_BOTTOM_LEFT_RADIUS = "border-bottom-left-radius";
    String BORDER_RIGHT_STYLE = "border-right-style";
    String BORDER_BOTTOM_STYLE = "border-bottom-style";
    String BORDER_LEFT_STYLE = "border-left-style";
    String BORDER_TOP_STYLE = "border-top-style";
    String BOX_SHADOW = "box-shadow";
    String SHADOW_QUALITY = "shadow-quality";

    String POSITION = "position";

    String KEEP_SCROLL_POSITION = "keep-scroll-position";

    String TEXT_DECORATION = "text-decoration";
    String TEXT_ALIGN = "text-align";
    String FONT_WEIGHT = "font-weight";
    String FONT_STYLE = "font-style";
    String FONT_SIZE = "font-size";
    String COLOR = "color";
    String LINES = "lines";
    String FONT_FAMILY = "font-family";
    String TEXT_OVERFLOW = "text-overflow";
    String ELLIPSIS = "ellipsis";
    String LINE_HEIGHT = "line-height";
    String DISABLED = "disabled";
    String VALUE = "value";
    String IMAGE_QUALITY = "image-quality";
    String FILTER = "filter";
    String QUALITY = "quality";
    String SRC = "src";
    String SOURCE = "source";
    String PLACE_HOLDER = "place-holder";
    String RESIZE_MODE = "resize-mode";
    String AUTO_RECYCLE = "auto-bitmap-recycle";
    String SHOW_INDICATORS = "show-indicators";
    String AUTO_PLAY = "auto-play";
    String SCROLL_DIRECTION = "scroll-direction";
    String SCOPE = "scope";
    String RECYCLE = "recycle";
    String LOADMORERETRY = "loadmoreretry";
    String LOADMOREOFFSET = "loadmoreoffset";
    String RECYCLE_IMAGE = "recycle-image";
    String LAYOUT = "layout";
    String SPAN_OFFSETS = "span-offsets";
    String COLUMN_WIDTH = "column-width";
    String COLUMN_COUNT = "column-count";
    String COLUMN_GAP = "column-gap";
    String SHOW_SCROLLBAR = "show-scrollbar";
    String LEFT_GAP= "leftGap";
    String RIGHT_GAP= "rightGap";
    String OVERFLOW = "overflow";
    String TYPE = "type";
    String PLACEHOLDER = "placeholder";
    String PLACEHOLDER_COLOR = "placeholder-color";
    String AUTOFOCUS = "autofocus";
    String SINGLELINE = "singleline";
    String MAX_LENGTH = "max-length";
    String MAXLENGTH = "maxlength";
    String ROWS = "rows";
    String CHECKED = "checked";
    String VISIBILITY = "visibility";
    String ITEM_COLOR = "item-color";
    String ITEM_SELECTED_COLOR = "item-selected-color";
    String ITEM_SIZE = "item-size";
    String DISPLAY = "display";
    String SHOW_LOADING = "show-loading";
    String SUFFIX = "suffix";
    String RESIZE = "resize";
    String IMAGE_SHARPEN = "image-sharpen";
    String SHARPEN = "sharpen";
    String PREFIX = "prefix";
    String INDEX = "index";
    String INTERVAL = "interval";
    String PLAY_STATUS = "play-status";
    String FONT_FACE = "font-face";
    String MAX = "max";
    String MIN = "min";
    String NAV_BAR_VISIBILITY = "hidden";
    String OFFSET_X_ACCURACY = "offset-x-accuracy";
    String OFFSET_X_RATIO = "offset-x-ratio";
    String ELEVATION = "elevation";
    String PERSPECTIVE = "perspective";
    String SCROLLABLE = "scrollable";
    String DRAGGABLE = "draggable";
    String DISTANCE_Y = "dy";
    String PULLING_DISTANCE = "pulling-distance";
    String VIEW_HEIGHT = "view-height";
    String PREVENT_MOVE_EVENT = "prevent-move-event";
    String SELECTION_START = "selection-start";
    String SELECTION_END = "selection-end";
    String OFFSET_ACCURACY = "offset-accuracy";
    String CONTENT_SIZE = "content-size";
    String CONTENT_OFFSET = "content-offset";
    String X = "x";
    String Y = "y";
    String RETURN_KEY_TYPE = "return-key-type";
    String OFFSET = "offset";
    String ANIMATED = "animated";
    String STABLE = "stable";
    String TRANSFORM = "transform";
    String TRANSFORM_ORIGIN = "transform-origin";
    String KEEP_INDEX = "keep-index";
    String KEEP_SELECTION_INDEX = "keep-selection-index";

    String INSERT_CELL_ANIMATION = "insert-animation";
    String DELETE_CELL_ANIMATION = "delete-animation";
    String AUTO = "auto";
    String NORMAL = "normal";
    String ARIA_LABEL = "aria-label";
    String ARIA_HIDDEN = "aria-hidden";
    String ROLE = "role";

    String LAYERLIMIT = "layer-limit";
    String LAYER_LIMIT = "layer-limit";

    String DIRECTION = "direction";
    String RTL = "rtl";

    String STICKY_OFFSET = "sticky-offset";
    String HAS_FIXED_SIZE = "has-fixed-size";
    String KEEP_POSITION_LAYOUT_DELAY = "keep-position-layout-delay";

    String OVERFLOW_HIDDEN_HEIGHT = "overflow-hidden-height";
    String OVERFLOW_HIDDEN_WIDTH = "overflow-hidden-width";

    String PRIORITY  = "priority";

    String STRATEGY  = "strategy";

    String ALLOW_COPY_PASTE = "allow-copy-paste";
    String INCLUDE_FONT_PADDING = "include-font-padding";
    String ENABLE_COPY = "enable-copy";

    interface  Recycler{
      String LIST_DATA = "list-data";
      String LIST_DATA_ITEM  ="alias";
      String LIST_DATA_ITEM_INDEX = "index";
      String LIST_DATA_TEMPLATE_SWITCH_KEY = "switch";
      String SLOT_TEMPLATE_CASE = "case";
      String SLOT_TEMPLATE_DEFAULT = "default";
      String CELL_INDEX = "cell-index";
      String TYPE_INDEX = "type-index";
    }

    String VIF_FALSE = "if-false";
    String UNDEFINED = "undefined";
    String FLAT = "flat";
    String RIPPLE_ENABLED = "ripple-enabled";

    String SHOULD_STOP_PROPAGATION_INIT_RESULT = "should-stop-propagation-init-result";
    String SHOULD_STOP_PROPAGATION_INTERVAL = "should-stop-propagation-interval";


    String NEST_SCROLLING_ENABLED = "nested-scrolling-enabled";

    String ORIENTATION  = "orientation";
  }

  public interface Value {

    int DENSITY = 3;
    int NAV_BAR_SHOWN = 0;
    int NAV_BAR_HIDDEN = 1;
    int AUTO = -1;
    int COLUMN_GAP_NORMAL = 32;
    int COLUMN_COUNT_NORMAL = 1;
    String MULTI_COLUMN = "multi-column";
    String GRID = "grid";
    String STICKY = "sticky";
    String FIXED = "fixed";
    String LEFT = "left";
    String RIGHT = "right";
    String CENTER = "center";
    String BOLD = "bold";
    String ITALIC = "italic";
    String ORIGINAL = "original";
    String LOW = "low";
    String NORMAL = "normal";
    String HIGH = "high";
    String VISIBLE = "visible";
    String HIDDEN = "hidden";
    String TEXT = "text";
    String PASSWORD = "password";
    String TEL = "tel";
    String EMAIL = "email";
    String URL = "url";
    String DATE = "date";
    String TIME = "time";
    String DATETIME = "datetime";
    String PLAY = "play";
    String PAUSE = "pause";
    String STOP = "stop";
    String DIRECTION_LEFT = "left";
    String DIRECTION_RIGHT = "right";
    String DIRECTION_UP = "up";
    String DIRECTION_DOWN = "down";
    String NUMBER = "number";

    String NONE = "none";
    String DEFAULT = "default";

    String HORIZONTAL = "horizontal";
  }

  public interface Event {

    String CLICK = "click";
    String APPEAR = "appear";
    String DISAPPEAR = "disappear";
    String LOADMORE = "loadmore";
    String FOCUS = "focus";
    String BLUR = "blur";
    String INPUT = "input";
    String VIEWAPPEAR = "viewappear";
    String VIEWDISAPPEAR = "viewdisappear";
    String START = "start";
    String PAUSE = "pause";
    String FINISH = "finish";
    String FAIL = "fail";
    String ERROR = "error";
    String RECEIVEDTITLE = "receivedtitle";
    String PAGEFINISH = "pagefinish";
    String PAGESTART = "pagestart";
    String ONREFRESH = "refresh";
    String ONLOADING = "loading";
    String ONLOAD = "load";
    String CHANGE = "change";
    String ONPULLING_DOWN = "pullingdown";
    String ONPULLING_UP = "pullingup";
    String SCROLL = "scroll";
    String SCROLL_START = "scrollstart";
    String SCROLL_END = "scrollend";
    String CLICKBACKITEM = "clickbackitem";
    String RESUME_EVENT = "WXApplicationDidBecomeActiveEvent";
    String PAUSE_EVENT = "WXApplicationWillResignActiveEvent";
    String RETURN = "return";
    String KEYBOARD = "keyboard";

    String UNSTICKY = "unsticky";
    String STICKY = "sticky";

    String ON_TRANSITION_END = "transitionEnd";

    String LAYEROVERFLOW = "layeroverflow";

    interface SLOT_LIFECYCLE {
      String CREATE = "create";
      String ATTACH = "attach";
      String DETACH = "detach";
      String DESTORY = "destroy";
    }

    String STOP_PROPAGATION = "stopPropagation";
    String STOP_PROPAGATION_RAX = "stoppropagation";
    String ONMESSAGE = "message";
    String NATIVE_BACK = "nativeback";
  }

  public interface PSEUDO {
    String ACTIVE = ":active";
    String ENABLED = ":enabled";
    String DISABLED = ":disabled";
    String FOCUS = ":focus";
  }

  public interface Scheme {

    String FILE = "file";
    String HTTPS = "https";
    String HTTP = "http";
    String LOCAL = "local";
    String DATA = "data";
  }

  public interface CodeCache {
    String URL = "bundleUrl";
    String DIGEST = "bundleDigest";
    String PATH = "codeCachePath";
    String BANNER_DIGEST = "digest";
    String SAVE_PATH = "v8";
  }

  public interface TimeFunction {
    String LINEAR = "linear";
    String EASE_IN_OUT = "ease-in-out";
    String EASE_IN = "ease-in";
    String EASE_OUT = "ease-out";
    String EASE = "ease";
    String CUBIC_BEZIER = "cubic-bezier";
  }
}
