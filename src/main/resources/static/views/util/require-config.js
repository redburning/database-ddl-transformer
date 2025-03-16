require = {
  	baseUrl: '/js',
  	paths: {
	    'vue': 'vue/vue@2.6.20.min',
	    'vue-router': 'vue/vue-router@3.0.1',
	    'vue-resource': 'vue/vue-resource@1.5.3.min',
	    'vuex': 'vue/vue-vuex@2.1.1',
	    'ELEMENT': 'elementui/element-ui@2.15.14.min',
	    'jquery': 'jquery/jquery@2.1.4.min',
	    'domReady': 'require/domReady@2.0.1',
	    'text': "require/text@2.0.15",
	    'css': 'require/require-css@0.1.2',
	    'prism': 'prism/prism@1.29.0'
  	},
  	shim: {
	    'vue-resource': {
	      	deps: ['vue']
	    },
	    'vue-router': {
	      	deps: ['vue']
	    },
	    'vuex': {
	      	deps: ['vue']
	    },
	    'ELEMENT': {
	      	deps: ['vue']
	    }
  	}
};