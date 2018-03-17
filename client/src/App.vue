<template>
<v-app>
  <v-content>
    <v-container fluid>
      <router-view/>
    </v-container>
  </v-content>
  <Loading :show="loading" :label="loadingLabel"></Loading>
</v-app>
</template>

<script>
import {
  mapState
} from 'vuex'
import Loading from 'vue-full-loading'
import type from '@/store/type'

export default {
  name: 'App',
  data() {
    return {
      loadingLabel: 'Loading...'
    }
  },
  computed: {
    ...mapState({
      loading: type.LOADING
    })
  },
  components: {
    Loading
  }
}
</script>
