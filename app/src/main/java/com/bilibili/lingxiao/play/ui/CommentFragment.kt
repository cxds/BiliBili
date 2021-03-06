package com.bilibili.lingxiao.play.ui

import android.support.design.widget.TabLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import com.bilibili.lingxiao.R
import com.bilibili.lingxiao.dagger.DaggerUiComponent
import com.bilibili.lingxiao.home.recommend.model.RecommendData
import com.bilibili.lingxiao.home.recommend.view.RecommendView
import com.bilibili.lingxiao.play.adapter.CommentAdapter
import com.bilibili.lingxiao.play.VideoPresenter
import com.bilibili.lingxiao.play.model.CommentData
import com.bilibili.lingxiao.play.model.VideoDetailData
import com.bilibili.lingxiao.play.model.VideoRecoData
import com.bilibili.lingxiao.utils.ToastUtil
import com.camera.lingxiao.common.app.BaseFragment
import com.camera.lingxiao.common.utills.LogUtils
import com.camera.lingxiao.common.utills.PopwindowUtil
import kotlinx.android.synthetic.main.fragment_comment.*
import kotlinx.android.synthetic.main.fragment_comment.view.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CommentFragment :BaseFragment(), RecommendView {
    private var videoPresenter = VideoPresenter(this, this)
    private lateinit var mAdapter: CommentAdapter
    private var mCommentList = arrayListOf<CommentData.Reply>()

    private var page = 1 //评论页数
    private var avNum = ""

    override val contentLayoutId: Int
        get() = R.layout.fragment_comment

    override fun initInject() {
        super.initInject()
        DaggerUiComponent.builder().build().inject(this)
    }

    override fun initWidget(root: View) {
        super.initWidget(root)
        var recommendManager = LinearLayoutManager(context)
        root.comment_recy.layoutManager = recommendManager
        mAdapter = CommentAdapter(mCommentList)
        root.comment_recy.adapter = mAdapter

        root.refresh.setOnRefreshListener({
            mCommentList.clear()
            videoPresenter.getComment(avNum,1)
        })
        root.refresh.setOnLoadMoreListener({
            page++
            videoPresenter.getComment(avNum,page)
        })
        var emptyView = View.inflate(context,R.layout.layout_empty,null)
        var image = emptyView.findViewById<ImageView>(R.id.image_error)
        image.setImageDrawable(resources.getDrawable(R.drawable.bilipay_common_error_tip))
        mAdapter.setEmptyView(emptyView)

        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            when(view.id){
                R.id.more ->{
                    val popwindowUtil = PopwindowUtil.PopupWindowBuilder(activity!!)
                        .setView(R.layout.pop_comment)
                        .setFocusable(true)
                        .setTouchable(true)
                        .setOutsideTouchable(true)
                        .create()
                    popwindowUtil.showAsDropDown(view,0,-view.getHeight());
                    popwindowUtil.getView<View>(R.id.pop_add_blacklist)!!.setOnClickListener {
                        popwindowUtil.dissmiss()
                    }
                    popwindowUtil.getView<View>(R.id.pop_report)!!.setOnClickListener {
                            v -> popwindowUtil.dissmiss()
                    }
                }
            }
        }
       
    }

    override fun isRegisterEventBus(): Boolean {
        return true
    }

    /**
     * 粘性事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public fun onGetVideoDetail(data: RecommendData) {
        avNum = data.param
        videoPresenter.getComment(avNum,page)
    }

    override fun onGetRecommendData(recommendData: List<RecommendData>) {

    }

    override fun onGetVideoDetail(videoDetailData: VideoDetailData) {

    }

    override fun onGetVideoRecommend(videoRecoData: VideoRecoData) {

    }
    //var hotView = View.inflate(context,R.layout.item_hot_segment,null)
    override fun onGetVideoComment(commentData: CommentData) {
        LogUtils.d("获取到评论："+commentData.toString())
        if (commentData.hots.size > 0 && page == 1){
            for (hot in commentData.hots){
                mAdapter.addData(hot)
            }
            var empty = commentData.hots.get(0)
            var e = empty.copy(viewType = CommentData.Reply.SEGMENT)
            mAdapter.addData(e)
            var tabView:TabLayout = (activity as PlayActivity).findViewById(R.id.skin_tabLayout)
            var tabLayout = tabView.getTabAt(1)
            tabLayout?.text = "评论 " + commentData.cursor.allCount
        }
        if (commentData.replies == null){
            refresh.finishRefresh()
            refresh.finishLoadMore()
        }else{
            mAdapter.addData(commentData.replies)
        }
        refresh.finishRefresh()
        refresh.finishLoadMore()
    }

    override fun showDialog() {

    }

    override fun diamissDialog() {

    }

    override fun showToast(text: String?) {
        ToastUtil.show(text)
        refresh.finishRefresh()
        refresh.finishLoadMore()
    }
}