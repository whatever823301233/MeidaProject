package com.qiang.meidaproject.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Qiang on 2016/3/24.
 *
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<RecyclerHolder> {

    /**
     * 上下文
     */
    private Context context;
    /**
     * 数据集合
     */
    private List<T> realDatas;

    /**
     * 主构造
     *
     * @param context
     *            上下文
     */
    public BaseAdapter(Context context) {
        this.context = context;
    }

    /**
     * 获取上下文
     *
     * @return 上下文
     */
    public Context getContext() {
        return context;
    }

    /**
     * 获取item对象
     *
     * @param position
     *            位置
     */
    public T getItem(int position) {
        if (null != realDatas && realDatas.size() > position) {
            return realDatas.get(position);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        if (null != realDatas) {
            return realDatas.size();
        }
        return 0;
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = createView(inflater, parent, viewType);
        return new RecyclerHolder(root, context);
    }

    /**
     * 创建item的根视图
     *
     * @param inflater
     *            LayoutInflater
     * @param parent
     *            父视图
     * @param viewType
     *            视图类型
     * @return item根视图
     */
    public abstract View createView(LayoutInflater inflater, ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        convert(holder, realDatas.get(position), position);
    }

    /**
     * Recycler适配器填充方法
     *
     * @param holder
     *            viewholder
     * @param item
     *            javabean
     */
    public abstract void convert(RecyclerHolder holder, T item, int position);

    /**
     * 设置数据
     *
     * @param datas
     *            数据
     * @param isLoadMore
     *            是否为加载更多
     * @return 适配器对象
     */
    public BaseAdapter<T> setDatas(Collection<T> datas, boolean isLoadMore) {
        if (isLoadMore) {
            if (null != datas && null != realDatas) {
                for (T t : datas) {
                    realDatas.add(t);
                }
            }
        } else {
            if (datas == null) {
                realDatas = new ArrayList<T>();
            } else if (datas instanceof List) {
                realDatas = (List<T>) datas;
            } else {
                realDatas = new ArrayList<T>(datas);
            }
        }
        return this;
    }
}
