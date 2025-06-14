import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    isLoggedIn: false
  }),
  getters: {
    isAdmin: state => state.user?.role === 'admin',
    isStaff: state => state.user?.role === 'staff'
  },
  actions: {
    login(userData) {
      this.user = userData
      this.isLoggedIn = true
    },
    logout() {
      this.user = null
      this.isLoggedIn = false
    }
  },

  // ✅ 加上這段就能存到 localStorage
  persist: {
    key: 'hotel-user',
    storage: localStorage
  }
})
