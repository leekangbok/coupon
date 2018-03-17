<template>
<v-container grid-list-md>
  <v-layout row wrap justify-center>
    <v-flex xs8>
      <v-form v-model="valid" ref="form" lazy-validation>
        <v-text-field label="E-mail" v-model="email" :rules="emailRules" clearable required></v-text-field>
        <v-btn @click="issueCoupon($event)" color="primary" :disabled="!valid" class="ml-0">쿠폰발급
          <v-icon dark right>check_circle</v-icon>
        </v-btn>
      </v-form>
    </v-flex>
    <v-flex xs8>
      <v-data-table :headers="headers" :items="items" :search="search" :pagination.sync="pagination" :total-items="totalItems" :loading="loading" :rows-per-page-items="[5,10,25]" class="elevation-1">
        <template slot="headerCell" slot-scope="props">
          <v-tooltip bottom>
            <span slot="activator">
              {{ props.header.text }}
            </span>
            <span>
              {{ props.header.text }}
            </span>
          </v-tooltip>
        </template>
        <template slot="items" slot-scope="props">
          <td>{{ props.item.email }}</td>
          <td>{{ props.item.couponId }}</td>
          <td>{{ props.item.date }}</td>
        </template>
      </v-data-table>
    </v-flex>
  </v-layout>
  <v-snackbar :timeout="snackbarTimeout" :color="snackbar.color" v-model="showSnackbar">
    {{ snackbar.text }}
    <v-btn dark flat @click.native="showSnackbar = false">닫기</v-btn>
  </v-snackbar>
</v-container>
</template>

<script>
import {
  mapState,
  mapMutations
} from 'vuex'
import type from '@/store/type'

export default {
  name: 'Coupon',
  data: () => ({
    snackbarTimeout: 3000,
    valid: false,
    email: '',
    emailRules: [
      v => !!v || '이메일주소를 입력하세요',
      v => /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/.test(v) || '잘못된 이메일 주소입니다'
    ],
    search: '',
    totalItems: 0,
    items: [],
    loading: true,
    pagination: {},
    headers: [{
        text: '이메일',
        value: 'email',
        sortable: false
      },
      {
        text: '쿠폰번호',
        value: 'couponId',
        sortable: false
      },
      {
        text: '발급날짜',
        value: 'date',
        sortable: false
      }
    ]
  }),
  created() {
    // this.fetchCoupon()
  },
  methods: {
    fetchCoupon() {
      const {
        page,
        rowsPerPage
      } = this.pagination
      this.loading = true
      this[type.GET_COUPON]({
          offset: (page - 1) * rowsPerPage,
          limit: rowsPerPage
        })
        .then(response => {
          this.totalItems = response.total
          this.items = response.items
          this.loading = false
        })
        .catch(error => {
          console.log(error)
        })
    },
    issueCoupon(event) {
      // event.stopPropagation()
      // event.preventDefault()
      this[type.ISSUE_COUPON]({
          email: this.email
        })
        .then(resolve => {
          this.$refs.form.reset()
          this.fetchCoupon()
        })
    },
    ...mapMutations({
      setShowSnackbar: type.SNACKBAR
    })
  },
  computed: {
    showSnackbar: {
      get() {
        return this.snackbar.show
      },
      set(value) {
        this.setShowSnackbar({
          show: value
        })
      }
    },
    ...mapState({
      snackbar: type.SNACKBAR
    })
  },
  watch: {
    pagination: {
      handler() {
        this.fetchCoupon()
      },
      deep: true
    }
  }
}
</script>
