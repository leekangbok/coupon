import Vue from 'vue'
import Vuex from 'vuex'
import type from './type'
import api from './api'

Vue.use(Vuex)

const state = {
  [type.COUPONS]: {
    total: 0,
    curr_total: 0,
    items: []
  },
  [type.LOADING]: false,
  [type.SNACKBAR]: {
    show: false,
    text: '',
    color: 'info'
  }
}

const mutations = {
  [type.LOADING](state, value) {
    state[type.LOADING] = value
  },
  [type.SNACKBAR](state, {
    text = '',
    color = 'info',
    show = false
  }) {
    state[type.SNACKBAR] = {
      text,
      color,
      show
    }
  }
}

const getters = {
  [type.COUPONS](state) {
    return state[type.COUPONS]
  },
  [type.LOADING](state) {
    return state[type.LOADING]
  },
  [type.SNACKBAR](state) {
    return state[type.SNACKBAR]
  }
}

const actions = {
  [type.GET_COUPON]({
    commit
  }, {
    offset = 0,
    limit = 40,
    email = ''
  } = {}) {
    commit(type.LOADING, true)
    return api.get({
      url: `/couponApi/coupon`,
      args: {
        params: {
          offset,
          limit,
          email
        }
      }
    }, {
      callback: (resolve, response) => {
        commit(type.LOADING, false)
        resolve(response)
      }
    })
  },
  [type.ISSUE_COUPON]({
    commit
  }, {
    email = ''
  }) {
    commit(type.LOADING, true)
    return api.post({
      url: `/couponApi/coupon`,
      args: {
        email
      }
    }, {
      callback: (resolve, response) => {
        commit(type.LOADING, false)
        commit(type.SNACKBAR, {
          ...codeTo(response.code),
          show: true
        })
        resolve(response)
      }
    })
  }
}

function codeTo(code) {
  if (code === 'SUCCESS') {
    return {text: '쿠폰이 발행되었습니다', color: 'success'}
  }
  if (code === 'EXISTS' || code === 'DUP') {
    return {text: '쿠폰발행이 완료된 이메일 주소입니다', color: 'error'}
  }
  return {text: '잘못된 입력값입니다', color: 'error'}
}

export default new Vuex.Store({state, mutations, actions, getters, modules: {}})
