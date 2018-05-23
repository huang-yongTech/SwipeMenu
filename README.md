"# SwipeMenu" 

自定义view项目学习，待完善

* 目前只实现了侧滑效果，集成到ListView上面没什么问题，但是集成到RecyclerView上面侧滑效果失效。
初步估计可能是跟RecyclerView的LayoutManager有关。
* 记录：配合ListView或者RecyclerView使用，当侧滑menu打开时出现数据错乱问题（当item1侧滑打开，
屏幕外的某个位置item也会打开）