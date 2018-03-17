import axios from 'axios'
import type from '@/store/type'
import {mapActions} from 'vuex'

const components = {}

function componentsInstall(Vue) {
  Object.keys(components).forEach(key => {
    Vue.component(`Iu${key}`, components[key])
  })
}

export default {
  install(Vue, options) {
    componentsInstall(Vue)

    Vue.prototype.$http = axios

    Vue.mixin({
      methods: {
        ...mapActions([type.GET_COUPON, type.ISSUE_COUPON])
      }
    })
  }
}
