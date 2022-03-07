import tkinter

from matplotlib.backends.backend_tkagg import (
    FigureCanvasTkAgg, NavigationToolbar2Tk)
# Implement the default Matplotlib key bindings.
from matplotlib.backend_bases import key_press_handler
from matplotlib.figure import Figure

import numpy as np
import sys

root = tkinter.Tk()
root.wm_title("推荐算法")

E1 = tkinter.Text(root)
E1.insert(tkinter.INSERT, sys.argv[4])

fig = Figure(figsize=(5, 4), dpi=100)
t = np.arange(0, 3, .01)
plt = fig.add_subplot(111)
# 画时间对比图
time = [float(sys.argv[1]), float(sys.argv[2]), float(sys.argv[3])]
#print(time)
labels = ['CIP', 'ECIP', 'ECLS']

plt.bar(range(len(time)), time, tick_label = labels)



canvas = FigureCanvasTkAgg(fig, master=root)  # A tk.DrawingArea.
canvas.draw()
canvas.get_tk_widget().pack(side=tkinter.TOP, fill=tkinter.BOTH, expand=1)

toolbar = NavigationToolbar2Tk(canvas, root)
toolbar.update()
canvas.get_tk_widget().pack(side=tkinter.TOP, fill=tkinter.BOTH, expand=1)


def on_key_press(event):
    print("you pressed {}".format(event.key))
    key_press_handler(event, canvas, toolbar)

canvas.mpl_connect("key_press_event", on_key_press)
E1.pack(side=tkinter.TOP)

def _quit():
    root.quit()     # stops mainloop
    root.destroy()  # this is necessary on Windows to prevent
                    # Fatal Python Error: PyEval_RestoreThread: NULL tstate


#button = tkinter.Button(master=root, text="Quit", command=_quit)
#button.pack(side=tkinter.BOTTOM)

# 居中
width = 500
height = 1000
screenwidth = root.winfo_screenwidth()
screenheight = root.winfo_screenheight()
alignstr = '%dx%d+%d+%d' % (width, height, (screenwidth-width)/2, (screenheight-height)/2)
root.geometry(alignstr)

tkinter.mainloop()
# If you put root.destroy() here, it will cause an error if the window is
# closed with the window manager.
