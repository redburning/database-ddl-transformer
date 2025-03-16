define(function(require){
	var vue = require("vue");
	require('css!/views/components/datasource/select/datasource-select.css');
	return vue.extend({
		template: require('text!/views/components/datasource/select/datasource-select.html'),
		props: {
			options: {
				type: Array,
				default: () => []
			},
			value: {
				type: Object,
				default: () => ({})
			}
		},
		data () {
			return {
				// 是否显示下拉选项
            	showSelectOptions: false,
            	// 显示全部的选项，还是过滤后的选项
            	showAllOptions: false,
                // 选择的选项
                selectedOption: this.value,
                // 输入的搜索内容
                inputContent: this.value.label ? this.value.label : ''
			}
		},
		computed: {
			filteredOptions () {
	            return this.options.filter(item => {
					if (this.inputContent) {
						return item.label.toLowerCase().includes(this.inputContent.toLowerCase())
					} else {
						return false;
					}
	            })
	        }
		},
		watch: {
			value: {
				handler(newVal) {
					this.selectedOption = newVal;
					this.inputContent = newVal ? newVal.label : '';
				},
				deep: true
			}
		},
		mounted() {
			document.addEventListener('click', this.handleClickOutside)
		},
		methods: {
			selectOption (option) {
				let change = false;
				if (this.selectedOption.key !== option.key) {
	            	change = true;
				}
	            this.selectedOption = option
	            this.showSelectOptions = false
	            this.inputContent = option.label
	            // 将选择的选项通知给父组件
	            this.$emit('input', option)
	            if (change) {
					// 触发change事件
	            	this.$emit('change', option)
				}
	        },
	        // 点击空白处选项列表消失
	        handleClickOutside (event) {
	            const inputWrapper = this.$el.querySelector('.input-wrapper')
	            const selectOptionList = this.$el.querySelector('.select-option-list')
	            if (inputWrapper && !inputWrapper.contains(event.target)) {
	                if (selectOptionList && !selectOptionList.contains(event.target)) {
	                    this.showSelectOptions = false
	                }
	            }
	        }
		}
	});
})