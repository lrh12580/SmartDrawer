#!/usr/bin/env python
# -*- coding: utf-8 -*-

from SocketServer import TCPServer, StreamRequestHandler
from datetime import datetime
import sqlite3
import json

HOST = ''
PORT = 21567
ADDR = (HOST, PORT)
DB = 'D:\server.db'
id_in_db = []
isChanged = True


class ReqHandler(StreamRequestHandler):
    SOURCE = 'source'
    RFID = 'rfid'
    CLIENT = 'client'

    def handle(self):
        json_str = self.rfile.readline().strip()
        print json_str
        json_dict = json.loads(json_str)
        jas = JAS()
        if json_dict[ReqHandler.SOURCE] == ReqHandler.RFID:
            json_list = [json_str]
            json_str = self.rfile.readline()
            while 'over' not in json_str:
                print json_str
                json_list.append(json_str)
                json_str = self.rfile.readline()
            print 'over'
            jas.mesgfrom_rfid(json_list)
        elif json_dict[ReqHandler.SOURCE] == ReqHandler.CLIENT:
            mesg = jas.mesgfrom_client(json_dict)
            if mesg:
                for string in mesg:
                    self.wfile.write(string+'\r\n')
            else:
                self.wfile.write('true')


def getfdate():
    return datetime.now().strftime('%Y-%m-%d %H:%M:%S')

# Jaon And Sqlite3
class JAS(object):
    CONTENTS = 'contents'
    HISTORY = 'history'

    ID = 'id'
    NAME = 'name'
    CATEGORY = 'category'
    EXIST = 'exist'
    OPTION = 'option'
    TIME = 'time'

    def __init__(self):
        self.conn = sqlite3.connect(DB)
        self.cur = self.conn.cursor()
        if not self.cur.execute('SELECT name FROM sqlite_master WHERE type="table"').fetchall():
            self.cur.execute('CREATE TABLE %s(' % JAS.CONTENTS +
                             '%s CHAR(20) PRIMARY KEY,' % JAS.ID +
                             '%s TEXT,' % JAS.NAME +
                             '%s int,' % JAS.EXIST +
                             '%s CHAR(20),' % JAS.DATE_IN +
                             '%s CHAR(20)' % JAS.DATE_OUT
                             )
            self.cur.execute('CREATE TABLE %s(' % JAS.HISTORY +
                             '%s CHAR(20) NOT NULL,' % JAS.ID +
                             '%s TEXT NOT NULL,' % JAS.OPTION +
                             '%s CHAR(20) NOT NULL' % JAS.TIME)
            self.conn.commit()


    def mesgfrom_rfid(self, json_list):
        global id_in_db, isChanged

        id_in_drawer = {json.loads(json_str)[JAS.ID] for json_str in json_list}
        all_ids = set(self.cur.execute('SELECT %s, %s FROM %s' % (JAS.ID, JAS.EXIST, JAS.CONTENTS)).fetchall())
        if not id_in_db:
            id_in_db = {id_tuple[0] for id_tuple in all_ids if id_tuple[1] == 1}

        out_set = id_in_db - id_in_drawer
        in_set = id_in_drawer - id_in_db

        if out_set == in_set:
            return

        for label_id in out_set:
            self.cur.execute('UPDATE %s ' % JAS.CONTENTS +
                             'SET %s=0 ' % JAS.EXIST +
                             'WHERE %s="%s"' % (JAS.ID, label_id)
                             )
            self._insert_history(label_id, u'取出抽屉', getfdate())

        id_in_db_0 = {id_tuple[0] for id_tuple in all_ids if id_tuple[1] == 0}


        for label_id in in_set:
            if label_id in id_in_db_0:
                self.cur.execute('UPDATE %s ' % JAS.CONTENTS +
                                 'SET %s=1 ' % JAS.EXIST +
                                 'WHERE %s="%s"' % (JAS.ID, label_id)
                                 )
            else:
                self.cur.execute('INSERT INTO contents '
                                 '(%s, %s, %s, %s) ' % (JAS.ID, JAS.NAME, JAS.CATEGORY, JAS.EXIST) +
                                 'VALUES '
                                 '("%s", "%s", "%s", %d)' % (label_id, u'未命名', u'未分类', 1)
                                 )
            self._insert_history(label_id, u'放入抽屉', getfdate())
        self.conn.commit()
        id_in_db = id_in_drawer
        isChanged = True


    def mesgfrom_client(self, json_dict):
        return getattr(self, json_dict[JAS.OPTION])(json_dict)

    def history(self, json_dict):
        return self._get_from_table(json_dict, JAS.HISTORY)[::-1]

    def update(self, json_dict):
        label_id = json_dict[JAS.ID]
        sets = ''
        for key, value in json_dict.items():
            if key == JAS.NAME or key == JAS.CATEGORY:
                sets += '%s="%s",' % (key, value)
                self._insert_history(label_id, u'%s 修改为 %s' % (u'名称' if key is 'name' else u'类别', value), getfdate())
        self.cur.execute('UPDATE %s SET ' % JAS.CONTENTS +sets[:-1]+' WHERE %s="%s"' % (JAS.ID, label_id))
        self.conn.commit()
        return None

    def delete(self, json_dict):
        self.cur.execute('DELETE FROM %s WHERE %s="%s"' % (JAS.CONTENTS, JAS.ID, json_dict[JAS.ID]))
        self._insert_history(json_dict[JAS.ID], '删除', getfdate())
        self.conn.commit()
        return None

    def get(self, json_dict):
        return self._get_from_table(json_dict, JAS.CONTENTS)

    def get_change(self, json_dict):
        global id_in_db, isChanged
        if isChanged:
            isChanged = False
            return ['{"id":"%s"}' % label_id for label_id in id_in_db]
        else:
            return ['{"id":"N"}']

    def _get_from_table(self, json_dict, table):
        lable_id = json_dict.get(JAS.ID)
        if lable_id:
            return [self.tuple_to_json(tuples) for tuples in self.cur.execute('SELECT * FROM %s WHERE %s="%s"' % (table, JAS.ID, lable_id)).fetchall()]
        else:
            return [self.tuple_to_json(tuples) for tuples in self.cur.execute('SELECT * FROM %s' % table).fetchall()]

    def _insert_history(self, label_id, option, date):
        self.cur.execute('INSERT INTO history '
                         '(%s, %s, %s) ' % (JAS.ID, JAS.OPTION, JAS.TIME) +
                         'VALUES '
                         '("%s", "%s", "%s")' % (label_id, option, date)
                         )

    @staticmethod
    def tuple_to_json(atuple):
        if len(atuple) == 3:
            return json.dumps({
                JAS.ID: atuple[0],
                JAS.OPTION: atuple[1],
                JAS.TIME: atuple[2]
            })
        return json.dumps({
            JAS.ID: atuple[0],
            JAS.NAME: atuple[1],
            JAS.CATEGORY: atuple[2],
            JAS.EXIST: atuple[3],
        })


tcpServer = TCPServer(ADDR, ReqHandler)
print 'waiting'
tcpServer.serve_forever()
