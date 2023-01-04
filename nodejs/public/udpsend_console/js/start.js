'use strict';

//const vConsole = new VConsole();
//const remoteConsole = new RemoteConsole("http://[remote server]/logio-post");
//window.datgui = new dat.GUI();

const base_url = "";

var vue_options = {
    el: "#top",
    mixins: [mixins_bootstrap],
    store: vue_store,
    data: {
        ipaddress: "192.168.1.227",
        port: 1234,
        param_toast: { type: "toast" },
        param_notification: { type: "notification" },
        param_free: "",
    },
    computed: {
    },
    methods: {
        send_toast: async function(){
            try{
                var result = await do_post(base_url + "/udpsend-send", { ipaddress: this.ipaddress, port: this.port, params : this.param_toast });
                console.log(result);
                localStorage.setItem("udpsend_ipaddress", this.ipaddress);
                localStorage.setItem("udpsend_port", this.port);
            }catch(error){
                console.error(error);
                alert(error);
            }
        },
        send_notification: async function(){
            try{
                var result = await do_post(base_url + "/udpsend-send",  { ipaddress: this.ipaddress, port: this.port, params : this.param_notification });
                console.log(result);
                localStorage.setItem("udpsend_ipaddress", this.ipaddress);
                localStorage.setItem("udpsend_port", this.port);
            }catch(error){
                console.error(error);
                alert(error);
            }
        },
        send_free: async function(){
            try{
                var message = JSON.parse(this.param_free);
                var result = await do_post(base_url + "/udpsend-send",  { ipaddress: this.ipaddress, port: this.port, params : message });
                console.log(result);
                localStorage.setItem("udpsend_ipaddress", this.ipaddress);
                localStorage.setItem("udpsend_port", this.port);
            }catch(error){
                console.error(error);
                alert(error);
            }
        },
    },
    created: function(){
    },
    mounted: function(){
        proc_load();

        this.ipaddress = localStorage.getItem("udpsend_ipaddress");
        this.port = localStorage.getItem("udpsend_port");
    }
};
vue_add_data(vue_options, { progress_title: '' }); // for progress-dialog
vue_add_global_components(components_bootstrap);
vue_add_global_components(components_utils);

/* add additional components */
  
window.vue = new Vue( vue_options );
