/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};

/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {

/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId])
/******/ 			return installedModules[moduleId].exports;

/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			exports: {},
/******/ 			id: moduleId,
/******/ 			loaded: false
/******/ 		};

/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);

/******/ 		// Flag the module as loaded
/******/ 		module.loaded = true;

/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}


/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;

/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;

/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";

/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(0);
/******/ })
/************************************************************************/
/******/ ({

/***/ 0:
/***/ function(module, exports, __webpack_require__) {

	__webpack_require__(156)
	var __weex_template__ = __webpack_require__(157)
	var __weex_style__ = __webpack_require__(158)
	var __weex_script__ = __webpack_require__(159)

	__weex_define__('@weex-component/01b832918f8c3165cebb597eb613f790', [], function(__weex_require__, __weex_exports__, __weex_module__) {

	    __weex_script__(__weex_module__, __weex_exports__, __weex_require__)
	    if (__weex_exports__.__esModule && __weex_exports__.default) {
	      __weex_module__.exports = __weex_exports__.default
	    }

	    __weex_module__.exports.template = __weex_template__

	    __weex_module__.exports.style = __weex_style__

	})

	__weex_bootstrap__('@weex-component/01b832918f8c3165cebb597eb613f790',undefined,undefined)

/***/ },

/***/ 153:
/***/ function(module, exports) {

	module.exports = {
	  "type": "div",
	  "classList": [
	    "slider-item-container"
	  ],
	  "children": [
	    {
	      "type": "image",
	      "classList": [
	        "slider-item-image"
	      ],
	      "attr": {
	        "src": function () {return this.image}
	      }
	    }
	  ]
	}

/***/ },

/***/ 154:
/***/ function(module, exports) {

	module.exports = {
	  "slider-item-container": {
	    "width": 542,
	    "height": 360,
	    "justifyContent": "center",
	    "flexDirection": "row",
	    "padding": 10
	  },
	  "slider-item-image": {
	    "width": 542,
	    "height": 360
	  }
	}

/***/ },

/***/ 155:
/***/ function(module, exports) {

	module.exports = function(module, exports, __weex_require__){'use strict';

	module.exports = {
	  data: function () {return {
	    image: '',
	    link: '',
	    href: ''
	  }},
	  methods: {
	    ready: function ready() {
	      this.href = this.link;
	    }
	  }
	};}
	/* generated by weex-loader */


/***/ },

/***/ 156:
/***/ function(module, exports, __webpack_require__) {

	var __weex_template__ = __webpack_require__(153)
	var __weex_style__ = __webpack_require__(154)
	var __weex_script__ = __webpack_require__(155)

	__weex_define__('@weex-component/slider-neighbor-item', [], function(__weex_require__, __weex_exports__, __weex_module__) {

	    __weex_script__(__weex_module__, __weex_exports__, __weex_require__)
	    if (__weex_exports__.__esModule && __weex_exports__.default) {
	      __weex_module__.exports = __weex_exports__.default
	    }

	    __weex_module__.exports.template = __weex_template__

	    __weex_module__.exports.style = __weex_style__

	})


/***/ },

/***/ 157:
/***/ function(module, exports) {

	module.exports = {
	  "type": "div",
	  "classList": [
	    "slider-page"
	  ],
	  "children": [
	    {
	      "type": "slider-neighbor-item",
	      "repeat": function () {return this.sliderItems}
	    }
	  ]
	}

/***/ },

/***/ 158:
/***/ function(module, exports) {

	module.exports = {
	  "slider-page": {
	    "width": 720,
	    "height": 420
	  }
	}

/***/ },

/***/ 159:
/***/ function(module, exports) {

	module.exports = function(module, exports, __weex_require__){"use strict";

	module.exports = {
	  data: function () {return {
	    items: [],
	    sliderItems: []
	  }},
	  methods: {
	    ready: function ready() {
	      this.sliderItems = this.getSliderItems();
	    },
	    getSliderItems: function getSliderItems() {
	      return this.items.map(function (item, index) {
	        return item;
	      }.bind(this));
	    }
	  }
	};}
	/* generated by weex-loader */


/***/ }

/******/ });