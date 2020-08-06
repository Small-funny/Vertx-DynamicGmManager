/**
 * type: loading 的类型，默认1
 * tipLabel: loading 内的文本，默认 loading...
 * wrap: loading 的父级
 * 
 * @param {*} config 传入对象（含type/tipLabel/wrap）
 */
function Loading(config) {
    this.type = config.type || 1;
    this.tipLabel = config.tipLabel || "loading...";
    this.wrap = config.wrap || document.body;
    this.loadingWrapper = null;
}

/* 初始化 loading 效果，在原型链上添加 init 方法 */
Loading.prototype.init = function () {
    this.createDom();
}

/* 创建 loading 结构 */
Loading.prototype.createDom = function () {
    // loading wrap的子盒子，即整个loading的内容盒子
    var loadingWrapper = document.createElement('div');
    loadingWrapper.className = 'loading-wrapper';
    // loading type对应的不同的动画
    var loadingView = document.createElement('div');
    loadingView.className = 'loading-view';
    // loading 内的文本标签
    var tipView = document.createElement('div');
    tipView.className = 'tip-view';
    tipView.innerText = this.tipLabel;
    // 对 loading type的三种情形进行判断
    switch (this.type) {
        case 1:
            html = `
                <div class="container1">
                    <div class="circle circle1"></div>
                    <div class="circle circle2"></div>
                    <div class="circle circle3"></div>
                    <div class="circle circle4"></div>
                </div>
                <div class="container2">
                    <div class="circle circle1"></div>
                    <div class="circle circle2"></div>
                    <div class="circle circle3"></div>
                    <div class="circle circle4"></div>
                </div>
            `;
            loadingView.innerHTML = html;
            break;
        case 2:
            var html = `
                <div class="bounce-view">
                    <div class="bounce bounce1"></div>
                    <div class="bounce bounce2"></div>
                    <div class="bounce bounce3"></div>
                </div>
           `;
            loadingView.innerHTML = html;
            break;
        case 3:
            var html = `
                <div class="wave">
                    <div class="react react1"></div>
                    <div class="react react2"></div>
                    <div class="react react3"></div>
                    <div class="react react4"></div>
                    <div class="react react5"></div>
                </div>
           `;
            loadingView.innerHTML = html;
            break;
        default:
            break;
    }
    loadingWrapper.appendChild(loadingView);
    loadingWrapper.appendChild(tipView);
    this.wrap.appendChild(loadingWrapper);
    this.loadingWrapper = loadingWrapper;
}
