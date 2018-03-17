import axios from 'axios'
import qs from 'qs'

function touchUrl(url) {
  if (url.endsWith('//')) {
    return url.slice(0, -1)
  }
  return url
}

export default {
  get({
    url,
    args = {}
  } = {}, {
    callback = (resolve, data) => {
      resolve(data)
    },
    errback = (reject, error) => {
      reject(error)
    }
  } = {}) {
    return new Promise((resolve, reject) => {
      axios.get(touchUrl(url), args).then(response => {
        callback(resolve, response.data)
      }).catch(error => {
        errback(reject, error)
      })
    })
  },
  post({
    url,
    args = {}
  }, {
    callback = (resolve, data) => {
      resolve(data)
    },
    errback = (reject, error) => {
      reject(error)
    }
  } = {}) {
    return new Promise((resolve, reject) => {
      axios.post(touchUrl(url), qs.stringify(args)).then(response => {
        callback(resolve, response.data)
      }).catch(error => {
        errback(reject, error)
      })
    })
  },
  delete({
    url,
    args = {}
  }, {
    callback = (resolve, data) => {
      resolve(data)
    },
    errback = (reject, error) => {
      reject(error)
    }
  } = {}) {
    return new Promise((resolve, reject) => {
      axios.delete(touchUrl(url), args).then(response => {
        callback(resolve, response.data)
      }).catch(error => {
        errback(reject, error)
      })
    })
  }
}
